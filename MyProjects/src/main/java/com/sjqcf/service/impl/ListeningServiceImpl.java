package com.sjqcf.service.impl;

import com.github.pagehelper.PageHelper;
import com.sjqcf.mapper.ListeningMapper;
import com.sjqcf.pojo.Audio;
import com.sjqcf.service.ListeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sjqcf.utils.SystemConstants.PAGE_SIZE;

@Service
public class ListeningServiceImpl implements ListeningService {

    @Autowired
    private ListeningMapper listeningMapper;

    @Override
    public List<Audio> getListeningList(Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return listeningMapper.getListeningList();
    }

    @Override
    public List<Audio> getListeningListByType(Integer pageNum, Integer type) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return listeningMapper.getListeningListByType(type);
    }

    @Override
    public Audio getById(Integer id) {
        return listeningMapper.getById(id);
    }

    @Override
    public String playAudio(Integer id) {
        // 先检查音频是否存在
        Audio audio = listeningMapper.getById(id);
        if (audio == null) {
            throw new RuntimeException("音频不存在");
        }
        // 返回音频URL用于播放
        return audio.getAudioUrl();
    }

    /**
     * 收藏音频
     */
    @Override
    public void favoriteAudio(Integer audioId, Integer userId) {
        // 检查音频是否存在
        Audio audio = listeningMapper.getById(audioId);
        if (audio == null) {
            throw new RuntimeException("音频不存在");
        }
        // 收藏音频
        listeningMapper.favoriteAudio(audioId, userId);
    }

    @Override
    public void cancelFavoriteAudio(Integer audioId, Integer userId) {
        // 检查音频是否存在
        Audio audio = listeningMapper.getById(audioId);
        if (audio == null) {
            throw new RuntimeException("音频不存在");
        }
        // 取消收藏音频
        listeningMapper.cancelFavoriteAudio(audioId, userId);
    }

    @Override
    public List<Audio> getFavoriteAudios(Integer userId, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return listeningMapper.getFavoriteAudios(userId);
    }

    @Override
    public boolean checkFavoriteAudio(Integer userId, Integer audioId) {
        return listeningMapper.checkFavoriteAudio(userId, audioId) > 0;
    }


}
