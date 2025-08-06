package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    // 根据用户的userId（String类型）查找用户套餐
    @Query("SELECT up FROM UserPackage up WHERE up.user.userId = :userId")
    List<UserPackage> findByUserUserId(@Param("userId") String userId);
    
    // 根据套餐ID查找用户套餐
    List<UserPackage> findByPkgId(Long packageId);
}
