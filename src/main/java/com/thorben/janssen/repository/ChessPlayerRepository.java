package com.thorben.janssen.repository;

import com.thorben.janssen.model.ChessPlayer;
import jakarta.data.Order;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import org.hibernate.query.restriction.Restriction;

import java.util.List;

@Repository
public interface ChessPlayerRepository extends CrudRepository<ChessPlayer, Long> {

    @Find
    List<ChessPlayer> findPlayers(Restriction<ChessPlayer> restriction, Order<ChessPlayer> order);
}
