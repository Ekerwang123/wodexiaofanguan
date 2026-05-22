package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.RestaurantTable;
import com.smallrestaurant.game.entity.Stove;
import com.smallrestaurant.game.entity.User;
import com.smallrestaurant.game.repository.RestaurantTableRepository;
import com.smallrestaurant.game.repository.StoveRepository;
import com.smallrestaurant.game.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    @Autowired
    private StoveRepository stoveRepository;
    @Transactional
    public User register(String username, String password, String nickname) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setNickname(nickname != null ? nickname : username);
        user.setBalance(0L);
        user.setRedPacket(java.math.BigDecimal.ZERO);
        user.setLikeCount(0);
        user.setTotalGuests(0);
        user.setExp(0);
        user.setLevel(1);
        user = userRepository.save(user);
        restaurantTableRepository.save(new RestaurantTable(user.getId(), 1, 1, true));
        restaurantTableRepository.save(new RestaurantTable(user.getId(), 1, 2, true));
        restaurantTableRepository.save(new RestaurantTable(user.getId(), 1, 3, true));
        stoveRepository.save(new Stove(user.getId(), 1, true));
        return user;
    }
    public User login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOptional.get();
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }
}
