package com.itsm.batch.job.asset;

import com.itsm.core.domain.asset.AssetHw;
import com.itsm.core.domain.asset.AssetSw;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetAutoRegisterJob {

    private final AssetHwRepository assetHwRepository;
    private final AssetSwRepository assetSwRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private static final Random RANDOM = new Random();

    // ── 운영장비 (INFRA_HW) 데이터 ──
    private static final Map<String, String[][]> INFRA_HW_DATA = new LinkedHashMap<>();
    static {
        INFRA_HW_DATA.put("SERVER_RACK", new String[][]{
                {"Dell", "PowerEdge R750"}, {"Dell", "PowerEdge R650"}, {"Dell", "PowerEdge R450"},
                {"HPE", "ProLiant DL380 Gen10"}, {"HPE", "ProLiant DL360 Gen10"},
                {"Lenovo", "ThinkSystem SR650 V2"}, {"Lenovo", "ThinkSystem SR630 V2"}
        });
        INFRA_HW_DATA.put("SERVER_BLADE", new String[][]{
                {"HPE", "Synergy 480 Gen10"}, {"Cisco", "UCS B200 M6"}, {"Dell", "PowerEdge MX750c"},
                {"HPE", "Synergy 660 Gen10"}, {"Cisco", "UCS B480 M5"}
        });
        INFRA_HW_DATA.put("SERVER_TOWER", new String[][]{
                {"Dell", "PowerEdge T550"}, {"Dell", "PowerEdge T350"},
                {"HPE", "ProLiant ML350 Gen10"}, {"HPE", "ProLiant ML110 Gen10"}
        });
        INFRA_HW_DATA.put("STORAGE_SAN", new String[][]{
                {"Dell EMC", "Unity XT 480"}, {"Dell EMC", "Unity XT 680"},
                {"NetApp", "AFF A250"}, {"NetApp", "AFF A400"},
                {"HPE", "Primera 630"}, {"HPE", "Primera 650"}
        });
        INFRA_HW_DATA.put("STORAGE_NAS", new String[][]{
                {"Synology", "RS3621xs+"}, {"QNAP", "TS-h1290FX"},
                {"Synology", "RS1221+"}, {"QNAP", "TS-h886"}
        });
        INFRA_HW_DATA.put("NETWORK_SWITCH", new String[][]{
                {"Cisco", "Catalyst 9300"}, {"Cisco", "Catalyst 9200"},
                {"Arista", "7050X3"}, {"Juniper", "EX4400"},
                {"HPE", "FlexNetwork 5130"}, {"Cisco", "Nexus 9336C-FX2"}
        });
        INFRA_HW_DATA.put("NETWORK_ROUTER", new String[][]{
                {"Cisco", "ISR 4431"}, {"Cisco", "ISR 4351"},
                {"Juniper", "MX204"}, {"HPE", "FlexNetwork MSR3620"}
        });
        INFRA_HW_DATA.put("NETWORK_FW", new String[][]{
                {"Palo Alto", "PA-5260"}, {"Palo Alto", "PA-3260"},
                {"Fortinet", "FortiGate 600E"}, {"Fortinet", "FortiGate 200F"},
                {"Check Point", "6200"}, {"Check Point", "6500"}
        });
        INFRA_HW_DATA.put("NETWORK_LB", new String[][]{
                {"F5", "BIG-IP i5800"}, {"Citrix", "ADC MPX 5905"},
                {"A10", "Thunder 3230S"}, {"F5", "BIG-IP i2800"}
        });
        INFRA_HW_DATA.put("NETWORK_AP", new String[][]{
                {"Cisco", "Catalyst 9120AX"}, {"Aruba", "AP-515"},
                {"Ruckus", "R750"}, {"Cisco", "Aironet 2802I"}
        });
        INFRA_HW_DATA.put("SECURITY_IDS", new String[][]{
                {"Cisco", "Firepower 4110"}, {"McAfee", "NS9300"},
                {"Trend Micro", "TippingPoint 8400TX"}
        });
        INFRA_HW_DATA.put("SECURITY_WAF", new String[][]{
                {"Imperva", "X8510"}, {"F5", "Advanced WAF"},
                {"Fortinet", "FortiWeb 1000E"}
        });
        INFRA_HW_DATA.put("POWER_UPS", new String[][]{
                {"APC", "Smart-UPS SRT 5000"}, {"APC", "Smart-UPS SRT 10000"},
                {"Eaton", "9PX 6000"}, {"CyberPower", "OL6000RT3U"}
        });
        INFRA_HW_DATA.put("POWER_PDU", new String[][]{
                {"APC", "AP8841"}, {"Raritan", "PX3-5902V"},
                {"Eaton", "ePDU G3"}
        });
        INFRA_HW_DATA.put("INFRA_KVM", new String[][]{
                {"Raritan", "Dominion KX III"}, {"ATEN", "KN4140VA"},
                {"Avocent", "MergePoint Unity"}
        });
    }

    // ── OA 자산 데이터 ──
    private static final Map<String, String[][]> OA_DATA = new LinkedHashMap<>();
    static {
        OA_DATA.put("OA_DESKTOP", new String[][]{
                {"Dell", "OptiPlex 7090"}, {"Dell", "OptiPlex 5090"},
                {"HP", "EliteDesk 800 G8"}, {"Lenovo", "ThinkCentre M90q"},
                {"HP", "ProDesk 400 G7"}
        });
        OA_DATA.put("OA_LAPTOP", new String[][]{
                {"Dell", "Latitude 5520"}, {"Dell", "Latitude 7420"},
                {"Lenovo", "ThinkPad T14s"}, {"Lenovo", "ThinkPad X1 Carbon Gen 9"},
                {"HP", "EliteBook 840 G8"}, {"HP", "ProBook 450 G8"}
        });
        OA_DATA.put("OA_MONITOR", new String[][]{
                {"Dell", "U2722D"}, {"Dell", "P2422H"},
                {"LG", "27UK850"}, {"Samsung", "S34J550"},
                {"Dell", "U2723QE"}
        });
        OA_DATA.put("OA_PRINTER", new String[][]{
                {"HP", "LaserJet Pro M404dn"}, {"Canon", "imageRUNNER C3530i"},
                {"Samsung", "SL-C1810W"}, {"HP", "Color LaserJet Pro M454dw"},
                {"Xerox", "VersaLink C405"}
        });
        OA_DATA.put("OA_PHONE", new String[][]{
                {"Cisco", "IP Phone 8845"}, {"Cisco", "IP Phone 7841"},
                {"Polycom", "VVX 450"}, {"Yealink", "T46U"}
        });
        OA_DATA.put("OA_TABLET", new String[][]{
                {"Apple", "iPad Pro 12.9"}, {"Apple", "iPad Air"},
                {"Samsung", "Galaxy Tab S8"}, {"Samsung", "Galaxy Tab A8"}
        });
        OA_DATA.put("OA_PERIPHERAL", new String[][]{
                {"Logitech", "MX Master 3"}, {"Dell", "WD19S Dock"},
                {"Jabra", "Evolve2 85"}, {"Logitech", "C920 Webcam"},
                {"Dell", "KB522 키보드"}
        });
        OA_DATA.put("OA_PROJECTOR", new String[][]{
                {"Epson", "EB-L610U"}, {"BenQ", "LU951ST"},
                {"Sony", "VPL-FHZ75"}, {"Epson", "EB-W52"}
        });
    }

    // ── 운영SW (INFRA_SW) 데이터 ──
    private static final Map<String, String[][]> INFRA_SW_DATA = new LinkedHashMap<>();
    static {
        // {swNm, version, swTypeCd}
        INFRA_SW_DATA.put("SW_OS", new String[][]{
                {"Red Hat Enterprise Linux", "8.8"}, {"Red Hat Enterprise Linux", "9.2"},
                {"Windows Server", "2022"}, {"Windows Server", "2019"},
                {"Ubuntu Server", "22.04 LTS"}, {"Ubuntu Server", "20.04 LTS"},
                {"CentOS", "7.9"}, {"Rocky Linux", "9.1"}
        });
        INFRA_SW_DATA.put("SW_DB", new String[][]{
                {"Oracle Database", "19c"}, {"Oracle Database", "21c"},
                {"PostgreSQL", "15.4"}, {"PostgreSQL", "14.9"},
                {"MySQL", "8.0.34"}, {"MS SQL Server", "2022"},
                {"MariaDB", "10.11"}
        });
        INFRA_SW_DATA.put("SW_WAS", new String[][]{
                {"Apache Tomcat", "10.1.13"}, {"Apache Tomcat", "9.0.80"},
                {"Nginx", "1.24.0"}, {"Apache HTTP Server", "2.4.57"},
                {"Oracle WebLogic", "14c"}, {"JBoss EAP", "7.4"}
        });
        INFRA_SW_DATA.put("SW_MIDDLEWARE", new String[][]{
                {"RabbitMQ", "3.12.4"}, {"Apache Kafka", "3.5.1"},
                {"Redis", "7.2.1"}, {"IBM MQ", "9.3"},
                {"Apache ActiveMQ", "5.18.2"}
        });
        INFRA_SW_DATA.put("SW_MONITORING", new String[][]{
                {"Zabbix", "6.4"}, {"Grafana Enterprise", "10.1"},
                {"Prometheus", "2.45"}, {"Datadog Agent", "7.47"},
                {"Nagios XI", "5.11"}
        });
        INFRA_SW_DATA.put("SW_BACKUP", new String[][]{
                {"Veeam Backup & Replication", "12"}, {"Acronis Cyber Backup", "15"},
                {"Veritas NetBackup", "10.1"}, {"Commvault Complete", "2023"}
        });
        INFRA_SW_DATA.put("SW_SECURITY", new String[][]{
                {"AhnLab V3 Endpoint Security", "9.0"}, {"CrowdStrike Falcon", "6.54"},
                {"Symantec Endpoint Protection", "14.3"}, {"Trend Micro Apex One", "2019"}
        });
        INFRA_SW_DATA.put("SW_VIRTUALIZATION", new String[][]{
                {"VMware vSphere", "8.0"}, {"Microsoft Hyper-V", "2022"},
                {"Proxmox VE", "8.0"}, {"Citrix Hypervisor", "8.2"}
        });
        INFRA_SW_DATA.put("SW_CONTAINER", new String[][]{
                {"Docker Enterprise", "24.0"}, {"Kubernetes", "1.28"},
                {"Red Hat OpenShift", "4.14"}, {"Rancher", "2.7"}
        });
        INFRA_SW_DATA.put("SW_CICD", new String[][]{
                {"Jenkins", "2.414"}, {"GitLab CI", "16.4"},
                {"ArgoCD", "2.8"}, {"GitHub Actions", "latest"}
        });
        INFRA_SW_DATA.put("SW_LICENSE", new String[][]{
                {"Microsoft 365 E3", "2023"}, {"Microsoft 365 E5", "2023"},
                {"Adobe Creative Cloud", "2024"}, {"AutoCAD", "2024"},
                {"Jira Software", "9.11"}
        });
    }

    // ── 위치 데이터 ──
    private static final String[] DC_LOCATIONS = {
            "DC-A 1F A1-R01", "DC-A 1F A1-R02", "DC-A 1F A1-R03", "DC-A 1F A1-R04", "DC-A 1F A1-R05",
            "DC-A 2F A2-R01", "DC-A 2F A2-R02", "DC-A 2F A2-R03", "DC-A 2F A2-R04", "DC-A 2F A2-R05",
            "DC-A 3F A3-R01", "DC-A 3F A3-R02", "DC-A 3F A3-R03", "DC-A 3F A3-R04", "DC-A 3F A3-R05",
            "DC-B 1F B1-R01", "DC-B 1F B1-R02", "DC-B 1F B1-R03", "DC-B 1F B1-R04", "DC-B 1F B1-R05",
            "DC-B 2F B2-R01", "DC-B 2F B2-R02", "DC-B 2F B2-R03", "DC-B 2F B2-R04", "DC-B 2F B2-R05"
    };

    private static final String[] OFFICE_LOCATIONS = {
            "본사 3F", "본사 4F", "본사 5F", "본사 6F", "본사 7F",
            "지사 2F", "지사 3F", "지사 4F"
    };

    // ── 제조사별 시리얼 접두사 ──
    private static final Map<String, String> SERIAL_PREFIX = Map.ofEntries(
            Map.entry("Dell", "DELL"), Map.entry("Dell EMC", "EMC"),
            Map.entry("HPE", "HPE"), Map.entry("HP", "HP"),
            Map.entry("Lenovo", "LNV"), Map.entry("Cisco", "CSC"),
            Map.entry("Arista", "ARS"), Map.entry("Juniper", "JNP"),
            Map.entry("Palo Alto", "PAN"), Map.entry("Fortinet", "FGT"),
            Map.entry("Check Point", "CPK"), Map.entry("F5", "F5N"),
            Map.entry("Citrix", "CTX"), Map.entry("A10", "A10"),
            Map.entry("Aruba", "ARB"), Map.entry("Ruckus", "RKS"),
            Map.entry("McAfee", "MCF"), Map.entry("Trend Micro", "TMC"),
            Map.entry("Imperva", "IMP"), Map.entry("APC", "APC"),
            Map.entry("Raritan", "RAR"), Map.entry("Eaton", "ETN"),
            Map.entry("CyberPower", "CPW"), Map.entry("ATEN", "ATN"),
            Map.entry("Avocent", "AVC"), Map.entry("Synology", "SYN"),
            Map.entry("QNAP", "QNP"), Map.entry("NetApp", "NTA"),
            Map.entry("Apple", "APL"), Map.entry("Samsung", "SMG"),
            Map.entry("LG", "LGE"), Map.entry("Canon", "CAN"),
            Map.entry("Xerox", "XRX"), Map.entry("Polycom", "PLY"),
            Map.entry("Yealink", "YLK"), Map.entry("Logitech", "LGT"),
            Map.entry("Jabra", "JBR"), Map.entry("Epson", "EPS"),
            Map.entry("BenQ", "BNQ"), Map.entry("Sony", "SNY")
    );

    public void execute() {
        log.info("[AssetAutoRegisterJob] 시작");

        List<Company> companies = companyRepository.findAll();
        List<User> users = userRepository.findAll();

        if (companies.isEmpty() || users.isEmpty()) {
            log.warn("회사 또는 사용자 데이터가 없어 자산 자동 등록을 건너뜁니다.");
            return;
        }

        long hwCount = assetHwRepository.count();

        if (hwCount < 50) {
            log.info("[AssetAutoRegisterJob] 초기 시딩 시작 (현재 HW: {}건)", hwCount);
            int infraHwCount = seedInfraHw(companies, users);
            int oaCount = seedOaAssets(companies, users);
            int swCount = seedInfraSw(companies, users);
            log.info("[AssetAutoRegisterJob] 초기 시딩 완료 - 운영장비: {}건, OA자산: {}건, 운영SW: {}건",
                    infraHwCount, oaCount, swCount);
        } else {
            log.info("[AssetAutoRegisterJob] 월간 자산 추가 시작");
            int added = addMonthlyAssets(companies, users);
            log.info("[AssetAutoRegisterJob] 월간 자산 추가 완료 - {}건", added);
        }

        log.info("[AssetAutoRegisterJob] 완료");
    }

    private int seedInfraHw(List<Company> companies, List<User> users) {
        int count = 0;
        int seq = 1;

        for (Map.Entry<String, String[][]> entry : INFRA_HW_DATA.entrySet()) {
            String subCategory = entry.getKey();
            String[][] models = entry.getValue();

            // 각 중분류별 최소 수량 결정
            int qty = getInfraHwQuantity(subCategory);

            for (int i = 0; i < qty; i++) {
                String[] model = models[RANDOM.nextInt(models.length)];
                String manufacturer = model[0];
                String modelNm = model[1];

                String assetNm = generateInfraHwName(subCategory, seq);
                String serialNo = generateSerialNo(manufacturer);
                String ipAddress = generateServerIp(seq);
                String macAddress = generateMacAddress();
                String location = pickRandom(DC_LOCATIONS);
                LocalDate introduced = randomPastDate(1, 5);
                LocalDate warrantyEnd = introduced.plusYears(RANDOM.nextInt(3) + 2);

                Company company = pickRandom(companies);
                User manager = pickRandom(users);

                AssetHw hw = AssetHw.builder()
                        .assetNm(assetNm)
                        .assetTypeCd(subCategory)
                        .assetCategory("INFRA_HW")
                        .assetSubCategory(subCategory)
                        .manufacturer(manufacturer)
                        .modelNm(modelNm)
                        .serialNo(serialNo)
                        .ipAddress(ipAddress)
                        .macAddress(macAddress)
                        .location(location)
                        .introducedAt(introduced)
                        .warrantyEndAt(warrantyEnd)
                        .company(company)
                        .manager(manager)
                        .status("ACTIVE")
                        .description(generateInfraHwDescription(subCategory, modelNm))
                        .build();

                assetHwRepository.save(hw);
                count++;
                seq++;
            }
        }
        return count;
    }

    private int seedOaAssets(List<Company> companies, List<User> users) {
        int count = 0;
        int seq = 1;

        for (Map.Entry<String, String[][]> entry : OA_DATA.entrySet()) {
            String subCategory = entry.getKey();
            String[][] models = entry.getValue();

            int qty = getOaQuantity(subCategory);

            for (int i = 0; i < qty; i++) {
                String[] model = models[RANDOM.nextInt(models.length)];
                String manufacturer = model[0];
                String modelNm = model[1];

                String assetNm = generateOaName(subCategory, seq);
                String serialNo = generateSerialNo(manufacturer);
                String ipAddress = needsIp(subCategory) ? generateOaIp(seq) : null;
                String macAddress = needsIp(subCategory) ? generateMacAddress() : null;
                String location = pickRandom(OFFICE_LOCATIONS);
                LocalDate introduced = randomPastDate(0, 4);
                LocalDate warrantyEnd = introduced.plusYears(RANDOM.nextInt(2) + 2);

                Company company = pickRandom(companies);
                User manager = pickRandom(users);

                AssetHw hw = AssetHw.builder()
                        .assetNm(assetNm)
                        .assetTypeCd(subCategory)
                        .assetCategory("OA")
                        .assetSubCategory(subCategory)
                        .manufacturer(manufacturer)
                        .modelNm(modelNm)
                        .serialNo(serialNo)
                        .ipAddress(ipAddress)
                        .macAddress(macAddress)
                        .location(location)
                        .introducedAt(introduced)
                        .warrantyEndAt(warrantyEnd)
                        .company(company)
                        .manager(manager)
                        .status("ACTIVE")
                        .description(generateOaDescription(subCategory, modelNm))
                        .build();

                assetHwRepository.save(hw);
                count++;
                seq++;
            }
        }
        return count;
    }

    private int seedInfraSw(List<Company> companies, List<User> users) {
        int count = 0;
        int seq = 1;

        for (Map.Entry<String, String[][]> entry : INFRA_SW_DATA.entrySet()) {
            String subCategory = entry.getKey();
            String[][] items = entry.getValue();

            int qty = getSwQuantity(subCategory);

            for (int i = 0; i < qty; i++) {
                String[] item = items[RANDOM.nextInt(items.length)];
                String swNm = item[0];
                String version = item[1];

                String licenseKey = generateLicenseKey();
                int licenseCnt = RANDOM.nextInt(50) + 1;
                LocalDate installed = randomPastDate(0, 3);
                LocalDate expired = installed.plusYears(RANDOM.nextInt(3) + 1);

                Company company = pickRandom(companies);
                User manager = pickRandom(users);

                AssetSw sw = AssetSw.builder()
                        .swNm(String.format("%s (서버%03d)", swNm, seq))
                        .swTypeCd(subCategory)
                        .assetCategory("INFRA_SW")
                        .assetSubCategory(subCategory)
                        .version(version)
                        .licenseKey(licenseKey)
                        .licenseCnt(licenseCnt)
                        .installedAt(installed)
                        .expiredAt(expired)
                        .company(company)
                        .manager(manager)
                        .status("ACTIVE")
                        .description(generateSwDescription(subCategory, swNm, version))
                        .build();

                assetSwRepository.save(sw);
                count++;
                seq++;
            }
        }
        return count;
    }

    private int addMonthlyAssets(List<Company> companies, List<User> users) {
        int hwToAdd = RANDOM.nextInt(2) + 1; // 1-2
        int swToAdd = RANDOM.nextInt(2);     // 0-1
        int added = 0;

        // 랜덤 HW 추가
        List<String> hwCategories = new ArrayList<>(INFRA_HW_DATA.keySet());
        hwCategories.addAll(OA_DATA.keySet());

        for (int i = 0; i < hwToAdd; i++) {
            String subCategory = pickRandom(hwCategories);
            boolean isOa = OA_DATA.containsKey(subCategory);
            String[][] models = isOa ? OA_DATA.get(subCategory) : INFRA_HW_DATA.get(subCategory);
            String[] model = models[RANDOM.nextInt(models.length)];

            String manufacturer = model[0];
            String modelNm = model[1];
            String category = isOa ? "OA" : "INFRA_HW";
            String location = isOa ? pickRandom(OFFICE_LOCATIONS) : pickRandom(DC_LOCATIONS);

            AssetHw hw = AssetHw.builder()
                    .assetNm(String.format("신규_%s_%s", subCategory, randomHex(4)))
                    .assetTypeCd(subCategory)
                    .assetCategory(category)
                    .assetSubCategory(subCategory)
                    .manufacturer(manufacturer)
                    .modelNm(modelNm)
                    .serialNo(generateSerialNo(manufacturer))
                    .ipAddress(isOa ? generateOaIp(RANDOM.nextInt(200) + 1) : generateServerIp(RANDOM.nextInt(200) + 1))
                    .macAddress(generateMacAddress())
                    .location(location)
                    .introducedAt(LocalDate.now())
                    .warrantyEndAt(LocalDate.now().plusYears(3))
                    .company(pickRandom(companies))
                    .manager(pickRandom(users))
                    .status("ACTIVE")
                    .description("월간 자동 추가 자산")
                    .build();

            assetHwRepository.save(hw);
            added++;
        }

        // 랜덤 SW 추가
        List<String> swCategories = new ArrayList<>(INFRA_SW_DATA.keySet());
        for (int i = 0; i < swToAdd; i++) {
            String subCategory = pickRandom(swCategories);
            String[][] items = INFRA_SW_DATA.get(subCategory);
            String[] item = items[RANDOM.nextInt(items.length)];

            AssetSw sw = AssetSw.builder()
                    .swNm(String.format("신규_%s_%s", item[0], randomHex(4)))
                    .swTypeCd(subCategory)
                    .assetCategory("INFRA_SW")
                    .assetSubCategory(subCategory)
                    .version(item[1])
                    .licenseKey(generateLicenseKey())
                    .licenseCnt(RANDOM.nextInt(20) + 1)
                    .installedAt(LocalDate.now())
                    .expiredAt(LocalDate.now().plusYears(1))
                    .company(pickRandom(companies))
                    .manager(pickRandom(users))
                    .status("ACTIVE")
                    .description("월간 자동 추가 소프트웨어")
                    .build();

            assetSwRepository.save(sw);
            added++;
        }

        return added;
    }

    // ── 수량 결정 메서드 ──

    private int getInfraHwQuantity(String subCategory) {
        return switch (subCategory) {
            case "SERVER_RACK" -> 20;
            case "SERVER_BLADE" -> 8;
            case "SERVER_TOWER" -> 5;
            case "STORAGE_SAN" -> 6;
            case "STORAGE_NAS" -> 4;
            case "NETWORK_SWITCH" -> 15;
            case "NETWORK_ROUTER" -> 6;
            case "NETWORK_FW" -> 8;
            case "NETWORK_LB" -> 4;
            case "NETWORK_AP" -> 10;
            case "SECURITY_IDS" -> 4;
            case "SECURITY_WAF" -> 3;
            case "POWER_UPS" -> 6;
            case "POWER_PDU" -> 8;
            case "INFRA_KVM" -> 3;
            default -> 3;
        };
    }

    private int getOaQuantity(String subCategory) {
        return switch (subCategory) {
            case "OA_DESKTOP" -> 50;
            case "OA_LAPTOP" -> 60;
            case "OA_MONITOR" -> 40;
            case "OA_PRINTER" -> 15;
            case "OA_PHONE" -> 20;
            case "OA_TABLET" -> 10;
            case "OA_PERIPHERAL" -> 15;
            case "OA_PROJECTOR" -> 5;
            default -> 5;
        };
    }

    private int getSwQuantity(String subCategory) {
        return switch (subCategory) {
            case "SW_OS" -> 10;
            case "SW_DB" -> 8;
            case "SW_WAS" -> 6;
            case "SW_MIDDLEWARE" -> 5;
            case "SW_MONITORING" -> 4;
            case "SW_BACKUP" -> 3;
            case "SW_SECURITY" -> 4;
            case "SW_VIRTUALIZATION" -> 3;
            case "SW_CONTAINER" -> 3;
            case "SW_CICD" -> 3;
            case "SW_LICENSE" -> 5;
            default -> 3;
        };
    }

    // ── 이름 생성 ──

    private String generateInfraHwName(String subCategory, int seq) {
        String prefix = switch (subCategory) {
            case "SERVER_RACK" -> "SRV-RACK";
            case "SERVER_BLADE" -> "SRV-BLD";
            case "SERVER_TOWER" -> "SRV-TWR";
            case "STORAGE_SAN" -> "STG-SAN";
            case "STORAGE_NAS" -> "STG-NAS";
            case "NETWORK_SWITCH" -> "NET-SW";
            case "NETWORK_ROUTER" -> "NET-RT";
            case "NETWORK_FW" -> "NET-FW";
            case "NETWORK_LB" -> "NET-LB";
            case "NETWORK_AP" -> "NET-AP";
            case "SECURITY_IDS" -> "SEC-IDS";
            case "SECURITY_WAF" -> "SEC-WAF";
            case "POWER_UPS" -> "PWR-UPS";
            case "POWER_PDU" -> "PWR-PDU";
            case "INFRA_KVM" -> "INF-KVM";
            default -> "INF";
        };
        return String.format("%s-%03d", prefix, seq);
    }

    private String generateOaName(String subCategory, int seq) {
        String prefix = switch (subCategory) {
            case "OA_DESKTOP" -> "PC";
            case "OA_LAPTOP" -> "NB";
            case "OA_MONITOR" -> "MON";
            case "OA_PRINTER" -> "PRT";
            case "OA_PHONE" -> "TEL";
            case "OA_TABLET" -> "TAB";
            case "OA_PERIPHERAL" -> "PER";
            case "OA_PROJECTOR" -> "PRJ";
            default -> "OA";
        };
        return String.format("%s-%04d", prefix, seq);
    }

    // ── 시리얼/IP/MAC 생성 ──

    private String generateSerialNo(String manufacturer) {
        String prefix = SERIAL_PREFIX.getOrDefault(manufacturer, "GEN");
        int year = LocalDate.now().getYear() - RANDOM.nextInt(5);
        return String.format("%s-%d-%s", prefix, year, randomHex(6).toUpperCase());
    }

    private String generateServerIp(int seq) {
        int subnet = (seq / 254) + 1;
        int host = (seq % 254) + 1;
        return String.format("10.10.%d.%d", Math.min(subnet, 5), host);
    }

    private String generateOaIp(int seq) {
        int subnet = (seq / 254) + 1;
        int host = (seq % 254) + 1;
        return String.format("192.168.%d.%d", Math.min(subnet, 10), host);
    }

    private String generateMacAddress() {
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256),
                RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    private String generateLicenseKey() {
        return String.format("%s-%s-%s-%s",
                randomHex(4).toUpperCase(), randomHex(4).toUpperCase(),
                randomHex(4).toUpperCase(), randomHex(4).toUpperCase());
    }

    // ── 설명 생성 ──

    private String generateInfraHwDescription(String subCategory, String modelNm) {
        return switch (subCategory) {
            case "SERVER_RACK", "SERVER_BLADE", "SERVER_TOWER" ->
                    String.format("데이터센터 운영 서버 - %s. CPU/메모리/디스크 구성에 따라 운영 서비스 배치.", modelNm);
            case "STORAGE_SAN", "STORAGE_NAS" ->
                    String.format("중앙 스토리지 시스템 - %s. 데이터 백업 및 아카이빙 용도.", modelNm);
            case "NETWORK_SWITCH", "NETWORK_ROUTER" ->
                    String.format("네트워크 인프라 장비 - %s. 코어/디스트리뷰션/액세스 레이어.", modelNm);
            case "NETWORK_FW" ->
                    String.format("보안 방화벽 - %s. 인바운드/아웃바운드 트래픽 필터링.", modelNm);
            case "NETWORK_LB" ->
                    String.format("로드밸런서 - %s. 서비스 트래픽 분산 처리.", modelNm);
            case "NETWORK_AP" ->
                    String.format("무선 액세스 포인트 - %s. 사무실 Wi-Fi 서비스 제공.", modelNm);
            case "SECURITY_IDS", "SECURITY_WAF" ->
                    String.format("보안 솔루션 장비 - %s. 침입 탐지 및 웹 방어.", modelNm);
            case "POWER_UPS", "POWER_PDU" ->
                    String.format("전원 관리 장비 - %s. 데이터센터 전원 안정성 보장.", modelNm);
            case "INFRA_KVM" ->
                    String.format("KVM 콘솔 - %s. 원격 서버 관리 용도.", modelNm);
            default -> String.format("인프라 장비 - %s", modelNm);
        };
    }

    private String generateOaDescription(String subCategory, String modelNm) {
        return switch (subCategory) {
            case "OA_DESKTOP" -> String.format("사무용 데스크톱 - %s. 업무용 PC.", modelNm);
            case "OA_LAPTOP" -> String.format("업무용 노트북 - %s. 사무실/재택 겸용.", modelNm);
            case "OA_MONITOR" -> String.format("모니터 - %s. 사무실 배치.", modelNm);
            case "OA_PRINTER" -> String.format("프린터/복합기 - %s. 부서 공용.", modelNm);
            case "OA_PHONE" -> String.format("IP 전화기 - %s. 내선/외선 통화 용도.", modelNm);
            case "OA_TABLET" -> String.format("태블릿 - %s. 현장 업무/프레젠테이션 용도.", modelNm);
            case "OA_PERIPHERAL" -> String.format("주변기기 - %s. 사무용.", modelNm);
            case "OA_PROJECTOR" -> String.format("프로젝터 - %s. 회의실 배치.", modelNm);
            default -> String.format("OA 자산 - %s", modelNm);
        };
    }

    private String generateSwDescription(String subCategory, String swNm, String version) {
        return switch (subCategory) {
            case "SW_OS" -> String.format("운영체제 - %s %s. 서버 기본 OS.", swNm, version);
            case "SW_DB" -> String.format("데이터베이스 - %s %s. 운영 데이터 관리.", swNm, version);
            case "SW_WAS" -> String.format("웹/WAS - %s %s. 애플리케이션 서비스.", swNm, version);
            case "SW_MIDDLEWARE" -> String.format("미들웨어 - %s %s. 메시지/캐시 서비스.", swNm, version);
            case "SW_MONITORING" -> String.format("모니터링 - %s %s. 인프라 상태 감시.", swNm, version);
            case "SW_BACKUP" -> String.format("백업 솔루션 - %s %s. 데이터 보호.", swNm, version);
            case "SW_SECURITY" -> String.format("보안 솔루션 - %s %s. 엔드포인트 보호.", swNm, version);
            case "SW_VIRTUALIZATION" -> String.format("가상화 - %s %s. 서버 가상화 플랫폼.", swNm, version);
            case "SW_CONTAINER" -> String.format("컨테이너 - %s %s. 컨테이너 오케스트레이션.", swNm, version);
            case "SW_CICD" -> String.format("CI/CD - %s %s. 빌드/배포 자동화.", swNm, version);
            case "SW_LICENSE" -> String.format("상용 라이선스 - %s %s. 볼륨 라이선스.", swNm, version);
            default -> String.format("운영 SW - %s %s", swNm, version);
        };
    }

    // ── 유틸 ──

    private boolean needsIp(String subCategory) {
        return "OA_DESKTOP".equals(subCategory) || "OA_LAPTOP".equals(subCategory)
                || "OA_PRINTER".equals(subCategory) || "OA_PHONE".equals(subCategory);
    }

    private LocalDate randomPastDate(int minYearsAgo, int maxYearsAgo) {
        int daysAgo = (minYearsAgo * 365) + RANDOM.nextInt((maxYearsAgo - minYearsAgo) * 365 + 1);
        return LocalDate.now().minusDays(daysAgo);
    }

    private String randomHex(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(RANDOM.nextInt(16)));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T pickRandom(T[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    private <T> T pickRandom(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
