package com.bit41.monolith;

import com.bit41.monolith.model.Product;
import com.bit41.monolith.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        productRepository.deleteAll();
        log.info("Seeding Nexbyte product catalogue with images...");

        List<Product> products = List.of(
            make("NX-AUD-001", "SoundWave Pro X",
                 "40mm drivers, ANC, 30-hour battery, premium foldable design",
                 "Audio", new BigDecimal("4999.00"), "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&q=80"),
            make("NX-AUD-002", "BassBeat Wireless",
                 "Deep bass, IPX5 water-resistant, 20-hour playback",
                 "Audio", new BigDecimal("2499.00"), "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=400&q=80"),
            make("NX-AUD-003", "ClearTone Earbuds",
                 "True wireless, 6mm micro-drivers, 24h case battery",
                 "Audio", new BigDecimal("1799.00"), "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400&q=80"),
            make("NX-AUD-004", "StudioMax Headphones",
                 "Studio-grade 50mm drivers, foldable, detachable cable",
                 "Audio", new BigDecimal("6999.00"), "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=400&q=80"),

            make("NX-WR-001", "FitPulse Smart Watch",
                 "Heart rate, SpO2, sleep tracking, 7-day battery",
                 "Wearables", new BigDecimal("3499.00"), "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&q=80"),
            make("NX-WR-002", "ProBand Ultra",
                 "GPS, always-on display, 14-day battery, sports modes",
                 "Wearables", new BigDecimal("7999.00"), "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400&q=80"),
            make("NX-WR-003", "RunTrack Lite",
                 "Step counter, calorie tracker, water-resistant band",
                 "Wearables", new BigDecimal("1299.00"), "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=400&q=80"),

            make("NX-PWR-001", "TurboCharge 20000",
                 "20000mAh, 65W PD fast charging, dual USB-C+A ports",
                 "Power", new BigDecimal("2299.00"), "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=400&q=80"),
            make("NX-PWR-002", "SlimPack 10000",
                 "10000mAh, 22.5W fast charging, pocket-size thin design",
                 "Power", new BigDecimal("1199.00"), "https://images.unsplash.com/photo-1615526675159-e248c3021d3f?w=400&q=80"),
            make("NX-PWR-003", "GaN Charger 65W",
                 "3-port GaN charger, USB-C+A, universal compatibility",
                 "Power", new BigDecimal("1899.00"), "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=400&q=80"),

            make("NX-CAM-001", "ActionCam 4K Pro",
                 "4K/60fps, EIS stabilization, waterproof to 10m",
                 "Cameras", new BigDecimal("8499.00"), "https://images.unsplash.com/photo-1512790182412-b19e6d62bc39?w=400&q=80"),
            make("NX-CAM-002", "DashCam HD",
                 "1080p loop recording, night vision, parking mode",
                 "Cameras", new BigDecimal("2999.00"), "https://images.unsplash.com/photo-1564466809058-bf4114d55352?w=400&q=80"),
            make("NX-CAM-003", "WebCam Ultra",
                 "2K 30fps, built-in ring light, plug-and-play USB",
                 "Cameras", new BigDecimal("3499.00"), "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=400&q=80"),

            make("NX-ACC-001", "ErgoMouse Pro",
                 "Vertical ergonomic design, 6-button, DPI 400-3200",
                 "Accessories", new BigDecimal("1599.00"), "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&q=80"),
            make("NX-ACC-002", "MechKeys Mini",
                 "65% compact layout, blue switches, RGB backlit",
                 "Accessories", new BigDecimal("3299.00"), "https://images.unsplash.com/photo-1595225476474-87563907a212?w=400&q=80"),
            make("NX-ACC-003", "USB-C Hub 7-in-1",
                 "4K HDMI, 3x USB-A, SD/MicroSD, 100W PD passthrough",
                 "Accessories", new BigDecimal("2199.00"), "https://images.unsplash.com/photo-1614187216773-a6428c0b9338?w=400&q=80"),
            make("NX-ACC-004", "Desk Mat XL",
                 "90x40cm non-slip mat, stitched edges, waterproof surface",
                 "Accessories", new BigDecimal("799.00"), "https://images.unsplash.com/photo-1542487354-feaf93476caa?w=400&q=80"),

            make("NX-HOME-001", "SmartPlug Pro",
                 "16A WiFi plug, energy monitoring, voice assistant support",
                 "Home", new BigDecimal("999.00"), "https://images.unsplash.com/photo-1558002038-1055907df827?w=400&q=80"),
            make("NX-HOME-002", "LED Strip 5m",
                 "RGB+W 5m strip, app control, music sync, cuttable",
                 "Home", new BigDecimal("1499.00"), "https://images.unsplash.com/photo-1550989460-0adf9ea622e2?w=400&q=80"),
            make("NX-HOME-003", "DeskLamp Glow",
                 "Eye-care LED, 5 colour temps, wireless charging base",
                 "Home", new BigDecimal("2499.00"), "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400&q=80")
        );

        productRepository.saveAll(products);
        log.info("Seeded {} Nexbyte products.", products.size());
    }

    private Product make(String sku, String name, String desc, String category, BigDecimal price, String imageUrl) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setDescription(desc);
        p.setCategory(category);
        p.setPrice(price);
        p.setImageUrl(imageUrl);
        return p;
    }
}
