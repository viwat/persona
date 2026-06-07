package com.example.persona.seeder;

import com.example.persona.search.dto.request.BillerSearch;
import com.example.persona.search.dto.request.GeoLocation;
import com.example.persona.search.dto.request.LocationSearch;
import com.example.persona.search.dto.request.PeopleSearch;
import com.example.persona.search.dto.request.ServiceSearch;
import com.example.persona.search.dto.request.TransactionSearch;
import com.example.persona.search.service.BillerSearchService;
import com.example.persona.search.service.LocationSearchService;
import com.example.persona.search.service.PeopleSearchService;
import com.example.persona.search.service.ServiceSearchService;
import com.example.persona.search.service.TransactionSearchService;
import com.example.persona.setting.repository.SettingRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SearchSeeder {

    private final TransactionSearchService transactionSearchService;
    private final ServiceSearchService serviceSearchService;
    private final PeopleSearchService peopleSearchService;
    private final LocationSearchService locationSearchService;
    private final BillerSearchService billerSearchService;
    private final SettingRepository settingRepository;

    @Bean
    @Profile("seed")
    public CommandLineRunner seedSearchData() {
        return args -> {
            log.info("Starting data seeding...");

            if (settingRepository.count() > 0) {
                log.info("Search seeder already run, skipping...");
                return;
            }

            log.info("Seeding transactions...");
            List<TransactionSearch> transactions = Arrays.asList(
                    TransactionSearch.builder()
                            ._id(UUID.randomUUID().toString())
                            .amount(new BigDecimal("100.50"))
                            .transactionId("TRX001")
                            .serviceType("DEPOSIT")
                            .serviceSubType("DEPOSIT")
                            .serviceName("Coffee shop purchase")
                            .title("Coffee shop purchase")
                            .description("Coffee shop purchase")
                            .imageUrl("https://via.placeholder.com/150")
                            .location("123 Main St, Anytown, USA")
                            .day("14")
                            .month("02")
                            .year("2025")
                            .customerKey("CK001")
                            .accountNo("ACC001")
                            .customerNo("CN001")
                            .deepLink("https://www.google.com")
                            .build(),
                    TransactionSearch.builder()
                            ._id(UUID.randomUUID().toString())
                            .amount(new BigDecimal("500.00"))
                            .transactionId("TRX002")
                            .serviceType("WITHDRAWAL")
                            .serviceSubType("WITHDRAWAL")
                            .serviceName("ATM withdrawal")
                            .title("ATM withdrawal")
                            .description("ATM withdrawal")
                            .imageUrl("https://via.placeholder.com/150")
                            .location("123 Main St, Anytown, USA")
                            .day("01")
                            .month("01")
                            .year("2025")
                            .customerKey("CK002")
                            .accountNo("ACC001")
                            .customerNo("CN001")
                            .deepLink("https://www.google.com")
                            .build(),
                    TransactionSearch.builder()
                            ._id(UUID.randomUUID().toString())
                            .amount(new BigDecimal("1000.00"))
                            .transactionId("TRX003")
                            .serviceType("TRANSFER")
                            .serviceSubType("TRANSFER")
                            .serviceName("Bank transfer")
                            .title("Bank transfer")
                            .description("Bank transfer to John Doe")
                            .imageUrl("https://via.placeholder.com/150")
                            .location("123 Main St, Anytown, USA")
                            .day("14")
                            .month("01")
                            .year("2025")
                            .customerKey("CK001")
                            .accountNo("ACC001")
                            .customerNo("CN001")
                            .deepLink("https://www.google.com")
                            .build());

            // Add transactions asynchronously
            transactions.forEach(transaction -> {
                try {
                    transactionSearchService.addTransaction(transaction).get();
                    log.info(
                            "Seeded transaction: {} - {} ({})",
                            transaction._id,
                            transaction.getServiceName(),
                            transaction.getAmount());
                } catch (Exception e) {
                    log.error("Error seeding transaction: {}", transaction._id, e);
                }
            });
            log.info("Completed seeding {} transactions", transactions.size());

            log.info("Seeding services...");
            List<ServiceSearch> services = Arrays.asList(
                    createService(
                            "TRANSFER",
                            "Fund Transfer",
                            "转账",
                            "PAYMENT",
                            "TRANSFER",
                            "P2P_TRANSFER",
                            "transfer,money,send",
                            "https://via.placeholder.com/150"),
                    createService(
                            "BILLPAY",
                            "Bill Payment",
                            "缴费",
                            "PAYMENT",
                            "BILL",
                            "UTILITY",
                            "bill,utility,payment",
                            "https://via.placeholder.com/150"),
                    createService(
                            "TOPUP",
                            "Mobile Top-up",
                            "充值",
                            "PAYMENT",
                            "TOPUP",
                            "MOBILE",
                            "mobile,phone,topup",
                            "https://via.placeholder.com/150"));

            services.forEach(service -> {
                try {
                    serviceSearchService.addService(service).get();
                    log.info("Seeded service: {} - {}", service.getCode(), service.getName());
                } catch (Exception e) {
                    log.error("Error seeding service: {}", service.getCode(), e);
                }
            });
            log.info("Completed seeding {} services", services.size());

            log.info("Seeding people...");
            List<PeopleSearch> people = Arrays.asList(
                    createPerson(
                            "CK001",
                            "CN001",
                            "ACC001",
                            "John Smith",
                            "John",
                            "Regular Customer",
                            "friend,regular,vip",
                            "https://via.placeholder.com/150"),
                    createPerson(
                            "CK002",
                            "CN002",
                            "ACC002",
                            "Sarah Johnson",
                            "Sarah",
                            "Premium Customer",
                            "friend,premium,vip",
                            "https://via.placeholder.com/150"),
                    createPerson(
                            "CK003",
                            "CN003",
                            "ACC003",
                            "Michael Chen",
                            "Mike",
                            "Business Customer",
                            "business,merchant,vip",
                            "https://via.placeholder.com/150"));

            people.forEach(person -> {
                try {
                    peopleSearchService.addPerson(person).get();
                    log.info("Seeded person: {} - {}", person.getCustomerKey(), person.getName());
                } catch (Exception e) {
                    log.error("Error seeding person: {}", person.getCustomerKey(), e);
                }
            });
            log.info("Completed seeding {} people", people.size());

            log.info("Seeding locations...");
            List<LocationSearch> locations = Arrays.asList(
                    createLocation(
                            1L,
                            "Central Mall ATM",
                            "Central Mall ATM",
                            "atm,withdrawal,cash",
                            "https://via.placeholder.com/150",
                            createGeoLocation(11.5564, 104.9282)),
                    createLocation(
                            2L,
                            "Central Mall ATM",
                            "Central Mall ATM",
                            "atm,withdrawal,cash",
                            "https://via.placeholder.com/150",
                            createGeoLocation(11.5564, 104.9282)),
                    createLocation(
                            3L,
                            "Central Mall ATM",
                            "Central Mall ATM",
                            "atm,withdrawal,cash",
                            "https://via.placeholder.com/150",
                            createGeoLocation(11.5564, 104.9282)));

            locations.forEach(location -> {
                try {
                    locationSearchService.addLocation(location).get();
                    log.info(
                            "Seeded location: {} - {} ({}, {})",
                            location.getLocationId(),
                            location.getLocationName(),
                            location.getGeo().getLatitude(),
                            location.getGeo().getLongitude());
                } catch (Exception e) {
                    log.error("Error seeding location: {}", location.getLocationId(), e);
                }
            });
            log.info("Completed seeding {} locations", locations.size());

            log.info("Seeding billers...");
            List<BillerSearch> billers = Arrays.asList(
                    createBiller(
                            "EDC001",
                            "Electricity Authority",
                            "អគ្គិសនីកម្ពុជា",
                            "柬埔寨电力公司",
                            "UTILITY",
                            "ELECTRICITY",
                            "electricity,utility,bill",
                            "https://via.placeholder.com/150"),
                    createBiller(
                            "WATER001",
                            "Water Supply Authority",
                            "រដ្ឋាករទឹកស្វយ័តក្រុងភ្នំពេញ",
                            "金边自来水公司",
                            "UTILITY",
                            "WATER",
                            "water,utility,bill",
                            "https://via.placeholder.com/150"),
                    createBiller(
                            "ISP001",
                            "Internet Service Provider",
                            "អ៊ីនធឺណិត",
                            "网络服务提供商",
                            "TELECOM",
                            "INTERNET",
                            "internet,isp,broadband",
                            "https://via.placeholder.com/150"));

            billers.forEach(biller -> {
                try {
                    billerSearchService.addBiller(biller).get();
                    log.info("Seeded biller: {} - {} ({})", biller.getCode(), biller.getName(), biller.getCategory());
                } catch (Exception e) {
                    log.error("Error seeding biller: {}", biller.getCode(), e);
                }
            });
            log.info("Completed seeding {} billers", billers.size());

            log.info("Data seeding completed successfully!");
        };
    }

    private ServiceSearch createService(
            String code,
            String nameEn,
            String nameZh,
            String serviceType,
            String serviceCategory,
            String serviceSubCategory,
            String tags,
            String imageUrl) {
        ServiceSearch service = new ServiceSearch();
        service.setCode(code);
        service.setName(nameEn);
        service.setNameKm(nameEn);
        service.setNameZh(nameZh);
        service.setServiceType(serviceType);
        service.setServiceCategory(serviceCategory);
        service.setServiceSubCategory(serviceSubCategory);
        service.setTags(tags);
        service.setImageUrl(imageUrl);
        service.setDeepLink("https://www.wingbank.com/" + code.toLowerCase());
        service.setMetadata("{}");
        return service;
    }

    private PeopleSearch createPerson(
            String customerKey,
            String customerNo,
            String accountNo,
            String name,
            String shortName,
            String serviceName,
            String tags,
            String profileUrl) {
        PeopleSearch person = new PeopleSearch();
        person.setCustomerNo(customerKey);
        person.setCustomerNo(customerNo);
        person.setAccountNo(accountNo);
        person.setName(name);
        person.setShortName(shortName);
        person.setServiceName(serviceName);
        person.setTags(tags);
        person.setProfileUrl(profileUrl);
        person.setDeepLink("https://www.wingbank.com/profile/" + customerKey.toLowerCase());
        person.setMetadata("{}");
        return person;
    }

    private LocationSearch createLocation(
            Long id, String name, String address, String tags, String imageUrl, GeoLocation geo) {
        LocationSearch location = new LocationSearch();
        location.setLocationId(id);
        location.setLocationName(name);
        location.setLocationAddress(address);
        location.setTags(tags);
        location.setImageUrl(imageUrl);
        location.setDeepLink("https://www.wingbank.com/location/" + id);
        location.setMetadata("{}");
        location.setGeo(geo);
        return location;
    }

    private BillerSearch createBiller(
            String code,
            String nameEn,
            String nameKm,
            String nameZh,
            String category,
            String subCategory,
            String tags,
            String imageUrl) {
        BillerSearch biller = new BillerSearch();
        biller.setCode(code);
        biller.setName(nameEn);
        biller.setNameKm(nameKm);
        biller.setNameZh(nameZh);
        biller.setCategory(category);
        biller.setSubCategory(subCategory);
        biller.setTags(tags);
        biller.setImageUrl(imageUrl);
        biller.setDeepLink("https://www.wingbank.com/biller/" + code.toLowerCase());
        biller.setMetadata("{}");
        return biller;
    }

    private GeoLocation createGeoLocation(double latitude, double longitude) {
        GeoLocation location = new GeoLocation();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}
