package peps.peps_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import peps.peps_back.items.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByLogin(String login);
}
