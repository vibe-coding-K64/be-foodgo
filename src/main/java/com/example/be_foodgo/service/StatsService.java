package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.DailyStatsDTO;
import com.example.be_foodgo.dto.StatsDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.StatsRepository;
import com.example.be_foodgo.repository.WalletRepository;
import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final WalletRepository walletRepository;
    private final StatsRepository statsRepository;

    public StatsService(WalletRepository walletRepository,
                        StatsRepository statsRepository) {
        this.walletRepository = walletRepository;
        this.statsRepository = statsRepository;
    }

    public StatsDTO getDriverStats(String userId) {
        log.info("Bat dau lay thong ke tong quan cua tai xe: {}", userId);
        try {
            Double balance = 0.0;
            Double totalEarned = 0.0;

            List<com.google.cloud.firestore.QueryDocumentSnapshot> walletSnapshots = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, "driver");
            if (!walletSnapshots.isEmpty()) {
                com.google.cloud.firestore.DocumentSnapshot walletDoc = walletSnapshots.get(0);
                balance = toDouble(walletDoc.get("balance"));
                totalEarned = toDouble(walletDoc.get("totalEarned"));
            }

            Long totalTrips = 0L;
            Double averageRating = 0.0;

            Map<String, Object> profileData = walletRepository.findDriverProfileById(userId);
            if (profileData != null) {
                totalTrips = toLong(profileData.get("totalTrips"));
                averageRating = toDouble(profileData.get("rating"));
            }

            Instant now = Instant.now();
            Instant todayStart = now.atZone(ZoneId.systemDefault()).toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant monthStart = now.atZone(ZoneId.systemDefault()).toLocalDate()
                    .withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            double todayEarnings = 0.0;
            long todayTrips = 0L;
            double monthEarnings = 0.0;
            long monthTrips = 0L;

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : walletRepository
                    .findAllDeliveryTransactionsByUserId(userId).get().getDocuments()) {
                Instant transTime = toInstant(doc.get("createdAt"));
                if (transTime == null) continue;
                double amount = toDouble(doc.get("amount"));
                if (!transTime.isBefore(todayStart)) todayEarnings += amount;
                if (!transTime.isBefore(monthStart)) monthEarnings += amount;
            }

            List<com.google.cloud.firestore.QueryDocumentSnapshot> orderDocs = statsRepository
                    .findByDriverIdAndStatus(userId, 3);
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : orderDocs) {
                Instant orderTime = toInstant(doc.get("updatedAt"));
                if (orderTime == null) continue;
                if (!orderTime.isBefore(todayStart)) todayTrips++;
                if (!orderTime.isBefore(monthStart)) monthTrips++;
            }

            return StatsDTO.builder()
                    .totalEarnings(totalEarned)
                    .balance(balance)
                    .totalTrips(totalTrips)
                    .averageRating(averageRating)
                    .todayEarnings(todayEarnings)
                    .todayTrips(todayTrips)
                    .monthEarnings(monthEarnings)
                    .monthTrips(monthTrips)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay thong ke: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay thong ke: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public List<DailyStatsDTO> getDriverDailyStats(String userId, String period, String date) {
        log.info("Bat dau lay thong ke theo ngay: userId={}, period={}, date={}", userId, period, date);
        try {
            LocalDate startDate;
            LocalDate endDate;

            if ("day".equalsIgnoreCase(period)) {
                startDate = LocalDate.parse(date);
                endDate = startDate;
            } else if ("month".equalsIgnoreCase(period)) {
                YearMonth ym = YearMonth.parse(date + "-01");
                startDate = ym.atDay(1);
                endDate = ym.atEndOfMonth();
            } else {
                throw new IllegalArgumentException("period phai la 'day' hoac 'month'");
            }

            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            Map<LocalDate, Double> earningsByDay = new HashMap<>();
            Map<LocalDate, Long> tripsByDay = new HashMap<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : walletRepository
                    .findAllDeliveryTransactionsByUserId(userId).get().getDocuments()) {
                Instant transTime = toInstant(doc.get("createdAt"));
                if (transTime == null) continue;
                if (!transTime.isBefore(startInstant) && transTime.isBefore(endInstant)) {
                    LocalDate day = transTime.atZone(ZoneId.systemDefault()).toLocalDate();
                    double amount = toDouble(doc.get("amount"));
                    earningsByDay.merge(day, amount, Double::sum);
                }
            }

            List<com.google.cloud.firestore.QueryDocumentSnapshot> orderDocs = statsRepository
                    .findByDriverIdAndStatus(userId, 3);
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : orderDocs) {
                Instant orderTime = toInstant(doc.get("updatedAt"));
                if (orderTime == null) continue;
                if (!orderTime.isBefore(startInstant) && orderTime.isBefore(endInstant)) {
                    LocalDate day = orderTime.atZone(ZoneId.systemDefault()).toLocalDate();
                    tripsByDay.merge(day, 1L, Long::sum);
                }
            }

            List<DailyStatsDTO> result = new ArrayList<>();
            LocalDate cursor = startDate;
            while (!cursor.isAfter(endDate)) {
                result.add(DailyStatsDTO.builder()
                        .date(cursor)
                        .earnings(earningsByDay.getOrDefault(cursor, 0.0))
                        .trips(tripsByDay.getOrDefault(cursor, 0L))
                        .build());
                cursor = cursor.plusDays(1);
            }

            log.info("Tra ve {} ngay thong ke", result.size());
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay thong ke theo ngay: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay thong ke theo ngay: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public Map<String, Object> getSystemStats() {
        log.info("Bat dau lay thong ke he thong cho Admin");
        try {
            com.google.cloud.firestore.Firestore firestore = walletRepository.getFirestore();

            long totalOrders = firestore.collection("orders").get().get().size();
            long totalStores = firestore.collection("stores").get().get().size();
            long totalDrivers = firestore.collection("users").whereEqualTo("role", 2).get().get().size();
            long totalCustomers = firestore.collection("users").whereEqualTo("role", 1).get().get().size();

            double totalRevenue = 0.0;
            List<com.google.cloud.firestore.QueryDocumentSnapshot> completedOrders = firestore.collection("orders")
                    .whereEqualTo("status", 3)
                    .get()
                    .get()
                    .getDocuments();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : completedOrders) {
                Double amount = doc.getDouble("totalAmount");
                if (amount != null) {
                    totalRevenue += amount;
                }
            }

            // Tinh weekly revenue
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            
            double[] weeklyRevenueArray = new double[7];
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : completedOrders) {
                Instant orderTime = toInstant(doc.get("createdAt"));
                if (orderTime == null) continue;
                java.time.LocalDate orderDate = orderTime.atZone(ZoneId.systemDefault()).toLocalDate();
                if (!orderDate.isBefore(monday) && !orderDate.isAfter(monday.plusDays(6))) {
                    int dayIndex = orderDate.getDayOfWeek().getValue() - 1; // 0 for Monday, 6 for Sunday
                    Double amount = doc.getDouble("totalAmount");
                    if (amount != null) {
                        weeklyRevenueArray[dayIndex] += amount;
                    }
                }
            }

            List<Double> weeklyRevenue = new ArrayList<>();
            for (double val : weeklyRevenueArray) {
                weeklyRevenue.add(val);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRevenue", totalRevenue);
            stats.put("totalOrders", totalOrders);
            stats.put("totalStores", totalStores);
            stats.put("totalDrivers", totalDrivers);
            stats.put("totalCustomers", totalCustomers);
            stats.put("weeklyRevenue", weeklyRevenue);

            return stats;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay thong ke he thong: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay thong ke he thong: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().toInstant();
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant();
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        return null;
    }
}
