package com.thorben.janssen;

import com.thorben.janssen.model.*;
import com.thorben.janssen.repository.ChessPlayerRepository;
import com.thorben.janssen.repository.ChessPlayerRepository_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.audit.AuditLog;
import org.hibernate.audit.AuditLogFactory;
import org.hibernate.query.Order;
import org.hibernate.query.SelectionQuery;
import org.hibernate.query.range.Range;
import org.hibernate.query.restriction.Path;
import org.hibernate.query.restriction.Restriction;
import org.hibernate.query.specification.SelectionSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestHibernate7 {

	Logger log = LogManager.getLogger(this.getClass().getName());

	private static EntityManagerFactory emf;

	private ChessPlayerRepository playerRepo;


	/**
	 * Stateless Session
	 */
	@Test
	public void testStatelessSession() {
		log.info("... testStatelessSession ...");

		StatelessSession statelessSession = emf.unwrap(SessionFactory.class).openStatelessSession();
		Transaction transaction = statelessSession.beginTransaction();

		ChessPlayer player = new ChessPlayer();
		player.setFirstName("Thorben");
		player.setLastName("Jansen");
		statelessSession.insert(player);

		player.setLastName("Janssen");
		statelessSession.update(player);

		transaction.commit();
		statelessSession.close();
	}

	@Test
	public void testStatelessSessionWith2ndLevelCache() {
		log.info("... testStatelessSessionWith2ndLevelCache ...");

		for (int i=0; i<2; i++) {
			StatelessSession statelessSession = emf.unwrap(SessionFactory.class).openStatelessSession();
			Transaction transaction = statelessSession.beginTransaction();

			statelessSession.setCacheMode(CacheMode.IGNORE);

			log.info("Fetch Player with id=1");
			ChessPlayer player = statelessSession.get(ChessPlayer.class, 1L);

			transaction.commit();
			statelessSession.close();
		}
	}

	@Test
	public void testStatelessSessionJdbcBatching() {
		log.info("... testStatelessSessionJdbcBatching ...");

		StatelessSession statelessSession = emf.unwrap(SessionFactory.class).openStatelessSession();
		Transaction transaction = statelessSession.beginTransaction();

		List<ChessPlayer> players = new ArrayList<>();
		for (int i=0; i<10; i++) {
			ChessPlayer player = new ChessPlayer();
			player.setFirstName("Player" + 1);

//			statelessSession.insert(player);
			players.add(player);
		}
		statelessSession.insertMultiple(players);

		transaction.commit();
		statelessSession.close();
	}

	/**
	 * SelectionSpecification
	 */

	@Test
	public void testSimpleRestriction() {
		log.info("... testSimpleRestriction ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		SelectionQuery<ChessPlayer> query = SelectionSpecification.create(ChessPlayer.class, "FROM ChessPlayer p")
														.restrict(Restriction.like(ChessPlayer_.firstName, "Mag%"))
//														.restrict(Restriction.startsWith(ChessPlayer_.firstName, "Thor"))
														.createQuery(em);
		var player = query.getResultList().get(0);
        log.info(player);

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testCombinedRestrictions() {
		log.info("... testCombinedRestrictions ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		SelectionQuery query = SelectionSpecification.create(ChessPlayer.class, "FROM ChessPlayer")
														.restrict(Restriction.like(ChessPlayer_.firstName, "Thor%")
																			.or(Restriction.equal(ChessPlayer_.firstName, "Paul")))
														.createQuery(em);
		query.getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testRangeRestrictions() {
		log.info("... testRangeRestrictions ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		SelectionQuery query = SelectionSpecification.create(ChessPlayer.class, "FROM ChessPlayer")
														.restrict(Path.from(ChessPlayer.class)
																		.to(ChessPlayer_.club)
																		.to(ChessClub_.name)
																		.restrict(Range.prefix("Local", false)))
														.createQuery(em);
		query.getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testRestrictionsWithOrdering() {
		log.info("... testRestrictionsWithOrdering ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		SelectionQuery query = SelectionSpecification.create(ChessPlayer.class, "FROM ChessPlayer p")
													  .restrict(Restriction.like(ChessPlayer_.firstName, "Thor%").or(Restriction.equal(ChessPlayer_.firstName, "Paul%")))
													  .sort(Order.asc(ChessPlayer_.lastName))
													  .createQuery(em);
		query.getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testRestrictionsWithDataRepository() {
		log.info("... testRestrictionsWithDataRepository ...");

		playerRepo.findPlayers(Restriction.like(ChessPlayer_.firstName, "Thor%")
						.or(Restriction.equal(ChessPlayer_.firstName, "Paul")),
				jakarta.data.Order.by(_ChessPlayer.lastName.asc()));
	}


	/**
	 * Hibernate 7.4
	 */

	@Test
	public void testJoinFetchLimit() {
		log.info("... testJoinFetchLimit ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		var clubs = em.createQuery("SELECT c FROM ChessClub c JOIN FETCH c.players", ChessClub.class).setMaxResults(2).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testTemporal() {
		log.info("... testTemporal ...");

		Long playerId;
		try (var em = emf.createEntityManager()) {
			em.getTransaction().begin();

			var player = new ChessPlayer();
			player.setFirstName("Magnus");
			player.setLastName("Carlsen");

			var club = new ChessClub();
			club.setName("St. Pauli");
			club.getPlayers().add(player);
			player.setClub(club);

			em.persist(club);
			em.persist(player);

			em.getTransaction().commit();
			playerId = player.getId();
		}

		var instant = Instant.now();

		try (var em = emf.createEntityManager()) {
			em.getTransaction().begin();

			var player = em.find(ChessPlayer.class, playerId);
			player.setFirstName("ChangedPlayerName");

			em.getTransaction().commit();
		}

		try (var session = emf.unwrap(SessionFactory.class)
				.withOptions()
				.asOf(instant)
				.open()) {
			session.getTransaction().begin();

			var player = session.find(ChessPlayer.class, playerId);
			log.info(player.toString());

			session.getTransaction().commit();
		}
	}


	@Test
	public void testAudited() {
		log.info("... testAudited ...");

		Long playerId;
		try (var em = emf.createEntityManager()) {
			em.getTransaction().begin();

			var player = new ChessPlayer();
			player.setFirstName("Magnus");
			player.setLastName("Carlsen");

			var club = new ChessClub();
			club.setName("St. Pauli");
			club.getPlayers().add(player);
			player.setClub(club);

			em.persist(club);
			em.persist(player);

			em.getTransaction().commit();
			playerId = player.getId();
		}

		var instant = Instant.now();

		try (var em = emf.createEntityManager()) {
			em.getTransaction().begin();

			var player = em.find(ChessPlayer.class, playerId);
			player.setFirstName("ChangedPlayerName");

			em.getTransaction().commit();
		}

		try (var auditLog = AuditLogFactory.create(emf)) {
			var changeSetId = auditLog.getChangesetId(instant);

			try (var session = emf.unwrap(SessionFactory.class)
					.withOptions()
					.atChangeset(changeSetId)
					.open()) {

				session.getTransaction().begin();

				var player = session.find(ChessPlayer.class, playerId);
				log.info(player.toString());

				session.getTransaction().commit();
			}
		}
	}

	@Test
	public void testAudited_Query() {
		log.info("... testAudited_Query ...");

		Long playerId;
		try (var em = emf.createEntityManager()) {
			em.getTransaction().begin();

			var player = new ChessPlayer();
			player.setFirstName("Magnus");
			player.setLastName("Carlsen");

			var club = new ChessClub();
			club.setName("St. Pauli");
			club.getPlayers().add(player);
			player.setClub(club);

			em.persist(club);
			em.persist(player);

			em.getTransaction().commit();
			playerId = player.getId();
		}

		var instant = Instant.now();

		try (var em = emf.createEntityManager()) {
			em.getTransaction().begin();

			var player = em.find(ChessPlayer.class, playerId);
			player.setFirstName("ChangedPlayerName");

			em.getTransaction().commit();
		}

		try (var session = emf.unwrap(SessionFactory.class)
				.withOptions()
				.atChangeset(AuditLog.ALL_CHANGESETS)
				.open()) {
			session.getTransaction().begin();

			var players = session.createQuery("SELECT p, changesetId(p) FROM ChessPlayer p WHERE p.id = :id", Object[].class).setParameter("id", playerId).getResultList();
			players.forEach(p -> log.info("Revision " + p[1] + ": " + p[0]));

			session.getTransaction().commit();
		}
	}

	/**
	 * Jakarta Persistence 3.2
	 */
	@Test
	public void testEmbeddableRecords() {
		log.info("... testEmbeddableRecords ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		ChessClub club = new ChessClub();
		club.setName("My Local Club");
		club.setAddress(new Address("Main Street", "My Local Town", "Here", "12345"));
		em.persist(club);

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testNamedQuery() {
		log.info("... testNamedQuery ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		List<ChessPlayer> players = em.createQuery(ChessPlayer_._findPlayersByFirstName_)
										.setParameter("firstName", "Magnus")
										.getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testEnumeratedValue() {
		log.info("... testEnumeratedValue ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		ChessPlayer player = new ChessPlayer();
		player.setFirstName("Thorben");
		player.setLastName("Janssen");
		player.setPlayerType(PlayerType.Hobby);
		em.persist(player);

		em.getTransaction().commit();
		em.close();
	}




	@BeforeAll
	public static void init() {
		emf = Persistence.createEntityManagerFactory("my-persistence-unit");
	}

	@BeforeEach
	public void before() {
		this.playerRepo = new ChessPlayerRepository_(emf.unwrap(SessionFactory.class).openStatelessSession());
	}

	@AfterAll
	public static void close() {
		emf.close();
	}
}
