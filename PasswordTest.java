import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码 "123456" 的BCrypt哈希
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hashedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 测试已知的BCrypt哈希
        String knownHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        boolean knownMatches = encoder.matches(password, knownHash);
        System.out.println("已知哈希验证结果: " + knownMatches);
        
        // 测试password123
        String password123 = "password123";
        String hash123 = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        boolean matches123 = encoder.matches(password123, hash123);
        System.out.println("password123验证结果: " + matches123);
    }
}