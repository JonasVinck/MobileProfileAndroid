package com.commeto.kuleuven.commetov2.sqlSupport;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Jonas on 2/03/2018.
 */

@Dao
public interface LocalRouteDAO {

    @Query("select * from localroute")
    List<LocalRoute> debug();

    @Query("select * from localroute where username = :username")
    List<LocalRoute> getAll(String username);

    @Query("select * from localroute where username = :username order by time desc")
    List<LocalRoute> getAllByTimeDescending(String username);

    @Query("select * from localroute where username = :username order by time asc")
    List<LocalRoute> getAllByTimeAscending(String username);

    @Query("select * from localroute where username = :username order by duration desc")
    List<LocalRoute> getAllByDurationDescending(String username);

    @Query("select * from localroute where username = :username order by duration asc")
    List<LocalRoute> getAllByDuratonAscending(String username);

    @Query("select * from localroute where username = :username order by ridename asc")
    List<LocalRoute> getAllByNameAscending(String username);

    @Query("select * from localroute where username = :username order by ridename desc")
    List<LocalRoute> getAllByNameDescending(String username);

    @Query("select * from localroute where username = :username order by speed asc")
    List<LocalRoute> getAllBySpeedAscending(String username);

    @Query("select * from localroute where username = :username order by speed desc")
    List<LocalRoute> getAllBySpeedDescending(String username);

    @Query("select * from localroute where username = :username order by distance asc")
    List<LocalRoute> getAllByDistanceAscending(String username);

    @Query("select * from localroute where username = :username order by distance desc")
    List<LocalRoute> getAllByDistanceDescending(String username);

    @Query("select * from localroute where localId = :id and username = :username")
    List<LocalRoute> exists(int id, String username);

    @Query("select * from localroute where id = :id and username = :username")
    List<LocalRoute> existsServerId(int id, String username);

    @Query("select * from localroute where sent = 0 and username = :username")
    List<LocalRoute> getAllNotSent(String username);

    @Query("select * from localroute where updated = 1 and username = :username")
    List<LocalRoute> getAllUpdated(String username);

    @Query("update LocalRoute set sent = :sent where id = :id")
    void updateSent(int id, boolean sent);

    @Query("update LocalRoute set ridename = :name where id = :id")
    void updateRideName(int id, String name);

    @Query("update LocalRoute set id = :serverId where id = :id")
    void updateRideId(int id, int serverId);

    @Query("delete from localroute")
    void deleteAll();

    @Update
    void update(LocalRoute... localRoutes);

    @Insert
    void insert(LocalRoute... localRoutes);

    @Delete
    void delete(LocalRoute... localRoutes);

//==================================================================================================
    //temp queries
}
