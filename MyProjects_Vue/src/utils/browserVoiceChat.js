import { getToken } from '@/utils/axios'

const INPUT_SAMPLE_RATE = 16000
const OUTPUT_SAMPLE_RATE = 24000
const CHUNK_BYTES = 640

export function buildVoiceWebSocketUrl(sessionId) {
  const token = getToken() || ''
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${window.location.host}/ws/voice?sessionId=${encodeURIComponent(sessionId)}&token=${encodeURIComponent(token)}`
}

function floatToS16Le640(chunkFloat320) {
  const out = new ArrayBuffer(CHUNK_BYTES)
  const view = new Int16Array(out)
  for (let i = 0; i < 320; i++) {
    let x = chunkFloat320[i]
    x = Math.max(-1, Math.min(1, x))
    view[i] = x < 0 ? x * 0x8000 : x * 0x7fff
  }
  return out
}

/**
 * 必须在用户点击的同一交互里尽快调用（在任意长 await 之前），
 * 否则 AudioContext 会一直处于 suspended，ScriptProcessor 不会触发，后端收不到麦克风数据。
 */
export async function prepareMicAudioContext() {
  const isLocalhost =
    window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
  const isSecure = window.location.protocol === 'https:' || isLocalhost

  if (!navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function') {
    if (!isSecure) {
      throw new Error(
        '麦克风需要安全访问：请使用 HTTPS 打开本站（IP/域名用 http:// 时浏览器不提供麦克风）。本地开发可用 https://localhost 或 http://localhost'
      )
    }
    throw new Error('当前浏览器不支持 getUserMedia，请换用 Chrome / Edge 最新版')
  }

  const stream = await navigator.mediaDevices.getUserMedia({
    audio: { channelCount: 1, echoCancellation: true, noiseSuppression: true }
  })

  let audioCtx
  try {
    audioCtx = new AudioContext({ sampleRate: INPUT_SAMPLE_RATE })
  } catch {
    audioCtx = new AudioContext()
  }

  if (audioCtx.state === 'suspended') {
    await audioCtx.resume()
  }

  return { stream, audioCtx }
}

/**
 * 在已取得 sessionId、且麦克风与 AudioContext 已在用户手势下 resume 之后调用。
 * @param {string} sessionId
 * @param {{ stream: MediaStream, audioCtx: AudioContext }} mic
 */
export async function startBrowserVoicePipeline(sessionId, mic) {
  const { stream, audioCtx } = mic

  const wsUrl = buildVoiceWebSocketUrl(sessionId)
  const ws = new WebSocket(wsUrl)
  ws.binaryType = 'arraybuffer'

  await new Promise((resolve, reject) => {
    ws.onopen = async () => {
      try {
        if (audioCtx.state === 'suspended') {
          await audioCtx.resume()
        }
      } catch (e) {
        console.warn('[voice] AudioContext.resume 在 WS 打开后失败', e)
      }
      resolve()
    }
    ws.onerror = () => reject(new Error('语音信道连接失败'))
  })

  if (audioCtx.state === 'suspended') {
    await audioCtx.resume()
  }

  const actualInRate = audioCtx.sampleRate
  const scale = actualInRate / INPUT_SAMPLE_RATE

  const micSource = audioCtx.createMediaStreamSource(stream)
  const bufferSize = 2048
  const processor = audioCtx.createScriptProcessor(bufferSize, 1, 1)

  let carry = new Float32Array(0)
  let processCount = 0
  let sentChunks = 0

  processor.onaudioprocess = (e) => {
    const outCh = e.outputBuffer.getNumberOfChannels()
    for (let c = 0; c < outCh; c++) {
      e.outputBuffer.getChannelData(c).fill(0)
    }

    if (ws.readyState !== WebSocket.OPEN) return

    processCount++
    if (processCount <= 3) {
      console.info('[voice] onaudioprocess #', processCount, 'ctxState=', audioCtx.state, 'ws=', ws.readyState)
    }

    const input = e.inputBuffer.getChannelData(0)
    const merged = new Float32Array(carry.length + input.length)
    merged.set(carry)
    merged.set(input, carry.length)

    let idx = 0
    while (idx + 320 * scale <= merged.length) {
      const chunk = new Float32Array(320)
      for (let k = 0; k < 320; k++) {
        chunk[k] = merged[Math.floor(idx + k * scale)]
      }
      idx += Math.floor(320 * scale)
      ws.send(floatToS16Le640(chunk))
      sentChunks++
      if (sentChunks === 1) {
        console.info('[voice] 已向服务器发送首块 PCM (640 字节), sampleRate=', actualInRate)
      }
    }
    carry = merged.slice(idx)
  }

  const mute = audioCtx.createGain()
  mute.gain.value = 0
  micSource.connect(processor)
  processor.connect(mute)
  mute.connect(audioCtx.destination)

  if (audioCtx.state === 'suspended') {
    await audioCtx.resume()
  }

  let playbackCtx = null
  let nextPlayTime = 0

  ws.onmessage = async (ev) => {
    if (!(ev.data instanceof ArrayBuffer)) return
    const int16 = new Int16Array(ev.data)
    if (int16.length === 0) return

    if (!playbackCtx) {
      try {
        playbackCtx = new AudioContext({ sampleRate: OUTPUT_SAMPLE_RATE })
      } catch {
        playbackCtx = new AudioContext()
      }
      nextPlayTime = playbackCtx.currentTime
      if (playbackCtx.state === 'suspended') {
        await playbackCtx.resume()
      }
    }

    const float32 = new Float32Array(int16.length)
    for (let i = 0; i < int16.length; i++) {
      float32[i] = int16[i] / 32768
    }

    const buf = playbackCtx.createBuffer(1, float32.length, playbackCtx.sampleRate)
    buf.getChannelData(0).set(float32)
    const src = playbackCtx.createBufferSource()
    src.buffer = buf
    src.connect(playbackCtx.destination)
    const startAt = Math.max(playbackCtx.currentTime, nextPlayTime)
    src.start(startAt)
    nextPlayTime = startAt + buf.duration
  }

  const stop = () => {
    console.info('[voice] 停止管道, 共发送 PCM 块=', sentChunks, 'audioprocess 次数=', processCount)
    try {
      ws.close()
    } catch {
      /* ignore */
    }
    try {
      processor.disconnect()
      micSource.disconnect()
      mute.disconnect()
      audioCtx.close()
    } catch {
      /* ignore */
    }
    try {
      if (playbackCtx) playbackCtx.close()
    } catch {
      /* ignore */
    }
    stream.getTracks().forEach((t) => t.stop())
  }

  return { stop, ws }
}
