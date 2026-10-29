package com.sjqcf.service;

import com.sjqcf.pojo.Audio;

import java.util.List;

public interface ListeningService {
    List<Audio> getListeningList(Integer pageNum);

    List<Audio> getListeningListByType(Integer pageNum, Integer type);

    Audio getById(Integer id);

    String playAudio(Integer id);

    void favoriteAudio(Integer audioId, Integer userId);

    void cancelFavoriteAudio(Integer audioId, Integer userId);

    List<Audio> getFavoriteAudios(Integer userId, Integer pageNum);

    boolean checkFavoriteAudio(Integer userId, Integer audioId);
}
