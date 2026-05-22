import com.autoatelier.model.Order;
import com.autoatelier.model.TuningService;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoAtelierTest {

    private List<TuningService> services;
    private List<Order>         orders;
    private Set<Long>           favorites;

    @BeforeEach
    void setUp() {
        services = List.of(
                makeService(1L, "Полировка кузова",    "Кузов и покраска", 15000.0, true),
                makeService(2L, "Чип-тюнинг двигателя","Двигатель",         25000.0, true),
                makeService(3L, "Шумоизоляция",        "Интерьер",          18000.0, true),
                makeService(4L, "Замена подвески",     "Подвеска",          32000.0, false)
        );
        orders = List.of(
                makeOrder(1L, "completed", 15000.0),
                makeOrder(2L, "completed", 25000.0),
                makeOrder(3L, "cancelled",  8000.0),
                makeOrder(4L, "new",        18000.0)
        );
        favorites = new HashSet<>();
    }
    @Test
    void tc01_filter_byCategory_returnsMatching() {
        String cat = "Двигатель";
        List<TuningService> result = services.stream()
                .filter(s -> cat.equals(s.getCategory()))
                .toList();
        assertEquals(1, result.size());
        assertEquals("Чип-тюнинг двигателя", result.get(0).getName());
    }

    @Test
    void tc02_filter_byNameSearch_caseInsensitive() {
        String q = "полир";
        List<TuningService> result = services.stream()
                .filter(s -> s.getName().toLowerCase().contains(q.toLowerCase()))
                .toList();
        assertEquals(1, result.size());
        assertEquals("Полировка кузова", result.get(0).getName());
    }

    @Test
    void tc03_filter_emptyQuery_returnsAll() {
        String q = "";
        List<TuningService> result = services.stream()
                .filter(s -> q.isBlank() || s.getName().toLowerCase().contains(q))
                .toList();
        assertEquals(4, result.size());
    }

    @Test
    void tc04_toggleFavorite_addsId() {
        assertFalse(favorites.contains(1L));
        toggleFavorite(1L);
        assertTrue(favorites.contains(1L));
    }

    @Test
    void tc05_toggleFavorite_removesOnSecondCall() {
        toggleFavorite(2L);
        assertTrue(favorites.contains(2L));
        toggleFavorite(2L);
        assertFalse(favorites.contains(2L));
    }

    @Test
    void tc06_filter_favorites_returnsOnlyFavorites() {
        toggleFavorite(1L);
        toggleFavorite(3L);
        List<TuningService> result = services.stream()
                .filter(s -> favorites.contains(s.getId()))
                .toList();
        assertEquals(2, result.size());
    }

    @Test
    void tc07_revenue_onlyCompleted() {
        double revenue = orders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                .sum();
        assertEquals(40000.0, revenue, 0.001);
    }

    @Test
    void tc08_stats_countByStatus() {
        Map<String, Long> stats = new HashMap<>();
        for (Order o : orders)
            stats.merge(o.getStatus(), 1L, Long::sum);
        assertEquals(2L, stats.getOrDefault("completed", 0L));
        assertEquals(1L, stats.getOrDefault("cancelled", 0L));
        assertEquals(1L, stats.getOrDefault("new", 0L));
    }

    private void toggleFavorite(long id) {
        if (favorites.contains(id)) favorites.remove(id);
        else favorites.add(id);
    }

    private TuningService makeService(long id, String name,
                                      String category, double price, boolean active) {
        TuningService s = new TuningService();
        s.setId(id); s.setName(name);
        s.setCategory(category); s.setPrice(price); s.setActive(active);
        return s;
    }

    private Order makeOrder(long id, String status, double price) {
        Order o = new Order();
        o.setId(id); o.setStatus(status); o.setTotalPrice(price);
        return o;
    }

}
