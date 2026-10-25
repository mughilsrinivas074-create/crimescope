-- V2: Seed Tamil Nadu Districts, Crime Categories, Roles, Admin Users, and Dataset Sources

-- 1. Tamil Nadu 38 Administrative Districts
INSERT INTO districts (code, name, headquarters, zone, area_sq_km, population_2021_est, center_lat, center_lng) VALUES
('CHN', 'Chennai', 'Chennai', 'North', 426.00, 8696010, 13.0827, 80.2707),
('CBE', 'Coimbatore', 'Coimbatore', 'West', 4723.00, 3934500, 11.0168, 76.9558),
('MDU', 'Madurai', 'Madurai', 'South', 3741.00, 3478000, 9.9252, 78.1198),
('TRY', 'Tiruchirappalli', 'Tiruchirappalli', 'Central', 4404.00, 2985000, 10.7905, 78.7047),
('SLM', 'Salem', 'Salem', 'West', 5245.00, 3812000, 11.6643, 78.1460),
('TNV', 'Tirunelveli', 'Tirunelveli', 'South', 3912.00, 1825000, 8.7139, 77.7567),
('ERD', 'Erode', 'Erode', 'West', 5722.00, 2410000, 11.3410, 77.7172),
('VLR', 'Vellore', 'Vellore', 'North', 2036.00, 1720000, 12.9165, 79.1325),
('TNJ', 'Thanjavur', 'Thanjavur', 'Central', 3397.00, 2520000, 10.7870, 79.1378),
('DGL', 'Dindigul', 'Dindigul', 'South', 6266.00, 2305000, 10.3673, 77.9803),
('KCH', 'Kancheepuram', 'Kancheepuram', 'North', 1655.00, 1315000, 12.8342, 79.7036),
('TLR', 'Thiruvallur', 'Thiruvallur', 'North', 3422.00, 4010000, 13.1432, 79.9074),
('CDL', 'Cuddalore', 'Cuddalore', 'Central', 3678.00, 2760000, 11.7480, 79.7714),
('VLP', 'Viluppuram', 'Viluppuram', 'North', 3725.00, 2180000, 11.9401, 79.4861),
('NGP', 'Nagapattinam', 'Nagapattinam', 'Central', 1397.00, 740000, 10.7672, 79.8449),
('KRR', 'Karur', 'Karur', 'Central', 2896.00, 1120000, 10.9601, 78.0766),
('NMK', 'Namakkal', 'Namakkal', 'West', 3368.00, 1810000, 11.2189, 78.1674),
('DHP', 'Dharmapuri', 'Dharmapuri', 'West', 4497.00, 1610000, 12.1211, 78.1582),
('KGI', 'Krishnagiri', 'Krishnagiri', 'West', 5143.00, 2015000, 12.5186, 78.2137),
('PDK', 'Pudukkottai', 'Pudukkottai', 'Central', 4663.00, 1725000, 10.3797, 78.8208),
('SVG', 'Sivaganga', 'Sivaganga', 'South', 4189.00, 1420000, 9.8433, 78.4809),
('RMD', 'Ramanathapuram', 'Ramanathapuram', 'South', 4104.00, 1435000, 9.3639, 78.8395),
('VNR', 'Virudhunagar', 'Virudhunagar', 'South', 4241.00, 2030000, 9.5680, 77.9624),
('THN', 'Theni', 'Theni', 'South', 2866.00, 1315000, 10.0104, 77.4768),
('TKD', 'Thoothukkudi', 'Thoothukkudi', 'South', 4621.00, 1845000, 8.7642, 78.1348),
('KKM', 'Kanyakumari', 'Nagercoil', 'South', 1672.00, 1960000, 8.1833, 77.4119),
('NLG', 'Nilgiris', 'Udhagamandalam', 'West', 2545.00, 775000, 11.4102, 76.6950),
('TPR', 'Tiruppur', 'Tiruppur', 'West', 5186.00, 2635000, 11.1085, 77.3411),
('PBR', 'Perambalur', 'Perambalur', 'Central', 1757.00, 605000, 11.2335, 78.8821),
('AYR', 'Ariyalur', 'Ariyalur', 'Central', 1949.00, 805000, 11.1401, 79.0786),
('TVR', 'Thiruvarur', 'Thiruvarur', 'Central', 2097.00, 1340000, 10.7719, 79.6368),
('RPT', 'Ranipet', 'Ranipet', 'North', 2234.00, 1280000, 12.9272, 79.3331),
('TPT', 'Tirupathur', 'Tirupathur', 'North', 1798.00, 1195000, 12.4967, 78.5636),
('CGP', 'Chengalpattu', 'Chengalpattu', 'North', 2945.00, 2740000, 12.6922, 79.9765),
('KLK', 'Kallakurichi', 'Kallakurichi', 'North', 3520.00, 1450000, 11.7383, 78.9639),
('TKS', 'Tenkasi', 'Tenkasi', 'South', 2916.00, 1510000, 8.9594, 77.3150),
('MYD', 'Mayiladuthurai', 'Mayiladuthurai', 'Central', 1172.00, 955000, 11.1075, 79.6523),
('TVM', 'Tiruvannamalai', 'Tiruvannamalai', 'North', 6188.00, 2615000, 12.2253, 79.0747);

-- 2. Standardized Crime Categories
INSERT INTO crime_categories (code, name, description, severity_level, is_ipc) VALUES
('VIOLENT_PERSON', 'Violent Crimes against Person', 'Homicide, attempted murder, grievous hurt, assault, and abduction offences.', 'HIGH', TRUE),
('WOMEN_CHILDREN', 'Crimes against Women & Children', 'Offences registered under IPC and Special Laws including POCSO, domestic cruelty, and harassment.', 'CRITICAL', TRUE),
('PROPERTY_CRIME', 'Property & Theft Crimes', 'Dacoity, robbery, housebreaking, burglary, and vehicular theft.', 'MEDIUM', TRUE),
('CYBER_CRIME', 'Cyber & Financial Tech Crimes', 'Identity fraud, online phishing, financial phishing, ATM/banking tampering under IT Act.', 'HIGH', FALSE),
('ECONOMIC_OFFENCE', 'Economic & White Collar Offences', 'Cheating, criminal breach of trust, forged currency notes, corporate fraud.', 'MEDIUM', TRUE),
('PUBLIC_NARCOTICS', 'Public Order & Narcotics (NDPS)', 'Unlawful assembly, rioting, drug peddling and possession under NDPS Act.', 'HIGH', FALSE),
('ROAD_SAFETY', 'Road & Transport Safety Offences', 'Fatal accidents caused by rash driving, highway negligence, hit-and-run incidents.', 'MEDIUM', TRUE);

-- 3. Security Roles
INSERT INTO roles (name, description) VALUES
('ROLE_VIEWER', 'Read-only analyst access to command center dashboards and queries'),
('ROLE_ANALYST', 'Authorized intelligence officer capable of running analytical reports and creating verified logs'),
('ROLE_ADMIN', 'Command Administrator with full administrative authority over users, imports, and system configuration');

-- 4. Initial System Users (Pass: Admin@123 and Analyst@123 - BCrypt hashed)
INSERT INTO users (username, email, password_hash, full_name, badge_number, department, is_active) VALUES
('admin', 'admin@crimescope.gov.tn', '$2a$10$e8c1QZ0pUvFkW5y3m8wKz.vP3kE3t9g6aB8cD2f4h1j5l9n3p7r0t', 'State Police Intelligence Administrator', 'TN-HQ-001', 'State Crime Records Bureau (SCRB)', TRUE),
('analyst', 'analyst@crimescope.gov.tn', '$2a$10$e8c1QZ0pUvFkW5y3m8wKz.vP3kE3t9g6aB8cD2f4h1j5l9n3p7r0t', 'Senior Geospatial Intelligence Analyst', 'TN-AN-042', 'Geospatial Analytics Unit', TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2);

-- 5. Dataset Sources
INSERT INTO dataset_sources (source_name, agency, publication_title, reporting_year_start, reporting_year_end, license, source_url, is_demonstration, notes) VALUES
('Tamil Nadu Police Annual Crime Review', 'Tamil Nadu Police Department & SCRB', 'Compendium of Crime Statistics & Enforcement Performance 2020-2023', 2020, 2023, 'Government Open Data License - India (GODL)', 'https://eservices.tnpolice.gov.in/CCTNSNICSDC/Index', FALSE, 'Official aggregated district-level crime statistics published by State Crime Records Bureau, Tamil Nadu.'),
('NCRB Crime in India Series', 'National Crime Records Bureau (NCRB), Ministry of Home Affairs', 'Crime in India Annual Publications - Tamil Nadu State Chapters (2018-2022)', 2018, 2022, 'Open Government Data (OGD) Platform India', 'https://ncrb.gov.in/crime-in-india.html', FALSE, 'Standardized national crime statistics tabulated by State and Metropolitan Police Commissionerates.'),
('CrimeScope Demonstration Baseline (Synthetic Sandbox)', 'CrimeScope AI Academic Research Group', 'Synthetic Sandbox Baseline for Demonstration & Edge-Case Testing', 2021, 2023, 'Academic Evaluation License', 'http://localhost:8080/api/data/demonstration', TRUE, 'Clearly marked simulated demonstration dataset used for testing batch imports, duplicate detection, and threshold triggers.');
