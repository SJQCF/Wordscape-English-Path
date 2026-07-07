package com.sjqcf.mapper;

import com.sjqcf.pojo.Audio;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ListeningMapper {

    List<Audio> getListeningList();

    List<Audio> getListeningListByType(Integer type);

    Audio getById(Integer id);

    void favoriteAudio(Integer audioId, Integer userId);

    void cancelFavoriteAudio(Integer audioId, Integer userId);

    List<Audio> getFavoriteAudios(Integer userId);

    Integer checkFavoriteAudio(Integer userId, Integer audioId);
}
