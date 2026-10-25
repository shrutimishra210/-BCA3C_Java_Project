package com.uniswap.config;

import com.uniswap.model.*;
import com.uniswap.repository.CategoryRepository;
import com.uniswap.repository.ListingRepository;
import com.uniswap.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs once on every startup. Creates the default category list if the
 * categories table is empty, and (first run only) a sample account plus
 * a handful of demo listings, so the app has something to show the
 * moment it's launched - exactly like the sample data banner printed by
 * the earlier SwapUni web project.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository,
                       UserRepository userRepository,
                       ListingRepository listingRepository,
                       PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCategories();
        seedSampleData();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;
        List<String> names = List.of("Books", "Electronics", "Stationery",
                "College Supplies", "Lab Equipment", "Other");
        names.forEach(n -> categoryRepository.save(new Category(n)));
        System.out.println("SwapUni: default categories created.");
    }

    private void seedSampleData() {
        if (userRepository.count() > 0) return;

        User amit = new User();
        amit.setFullName("Amit Sharma");
        amit.setEmail("amit.sharma@cgc.edu.in");
        amit.setUsername("amit.sharma");
        amit.setPasswordHash(passwordEncoder.encode("Password@123"));
        amit.setCourse("B.Tech CSE");
        amit.setYear("3rd Year");
        amit.setCampus("Block A Hostel");
        amit.setBio("Trading textbooks for coffee money.");
        userRepository.save(amit);

        Category books = categoryRepository.findByName("Books").orElseThrow();
        Category electronics = categoryRepository.findByName("Electronics").orElseThrow();

        Listing l1 = new Listing();
        l1.setName("Engineering Mathematics Book");
        l1.setDescription("Good condition, all chapters intact, minimal highlighting.");
        l1.setCategory(books);
        l1.setPrice(320.0);
        l1.setCondition(ItemCondition.GOOD);
        l1.setTransactionType(TransactionType.SELL);
        l1.setOwner(amit);
        l1.setLocation("Library Block");
        listingRepository.save(l1);

        Listing l2 = new Listing();
        l2.setName("Scientific Calculator (Casio fx-991)");
        l2.setDescription("Barely used, all functions working.");
        l2.setCategory(electronics);
        l2.setPrice(500.0);
        l2.setCondition(ItemCondition.LIKE_NEW);
        l2.setTransactionType(TransactionType.SELL);
        l2.setOwner(amit);
        l2.setLocation("Academic Block");
        listingRepository.save(l2);

        System.out.println("=================================================");
        System.out.println("SwapUni sample data loaded successfully!");
        System.out.println("Sample login: amit.sharma@cgc.edu.in / Password@123");
        System.out.println("=================================================");
    }
}
