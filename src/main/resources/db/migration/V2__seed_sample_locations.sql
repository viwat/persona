-- V2__seed_sample_locations.sql
-- Sample Wing Bank locations for development/testing

INSERT INTO locations (id, name, type, status, latitude, longitude, street, commune, district, province, phone, opening_hours, available_services)
VALUES
    (
        gen_random_uuid(),
        'Wing Bank Phnom Penh Head Office',
        'branch',
        'ACTIVE',
        11.5564, 104.9282,
        'No. 721, Preah Monivong Blvd',
        'Tonle Bassac',
        'Chamkarmorn',
        'Phnom Penh',
        '+855 23 999 989',
        '{"schedule": {"MONDAY": {"openTime": "08:00", "closeTime": "17:00"}, "TUESDAY": {"openTime": "08:00", "closeTime": "17:00"}, "WEDNESDAY": {"openTime": "08:00", "closeTime": "17:00"}, "THURSDAY": {"openTime": "08:00", "closeTime": "17:00"}, "FRIDAY": {"openTime": "08:00", "closeTime": "17:00"}, "SATURDAY": {"openTime": "08:00", "closeTime": "12:00"}}, "specialNotes": "Closed on public holidays"}',
        '["Cash Deposit", "Cash Withdrawal", "Account Opening", "Loan Services", "Foreign Exchange"]'
    ),
    (
        gen_random_uuid(),
        'Wing ATM Toul Tom Poung',
        'atm_crm',
        'ACTIVE',
        11.5431, 104.9196,
        'Russian Market, St 163',
        'Toul Tom Poung I',
        'Chamkarmorn',
        'Phnom Penh',
        NULL,
        '{"specialNotes": "24/7"}',
        '["Cash Withdrawal", "Balance Inquiry", "Mini Statement"]'
    ),
    (
        gen_random_uuid(),
        'Wing Agent - Siem Reap Market',
        'agent',
        'ACTIVE',
        13.3621, 103.8557,
        'Psar Leu Market',
        'Svay Dankum',
        'Siem Reap',
        'Siem Reap',
        '+855 63 963 963',
        '{"schedule": {"MONDAY": {"openTime": "07:00", "closeTime": "18:00"}, "TUESDAY": {"openTime": "07:00", "closeTime": "18:00"}, "WEDNESDAY": {"openTime": "07:00", "closeTime": "18:00"}, "THURSDAY": {"openTime": "07:00", "closeTime": "18:00"}, "FRIDAY": {"openTime": "07:00", "closeTime": "18:00"}, "SATURDAY": {"openTime": "07:00", "closeTime": "18:00"}, "SUNDAY": {"openTime": "08:00", "closeTime": "12:00"}}}',
        '["Cash In", "Cash Out", "Bill Payment"]'
    ),
    (
        gen_random_uuid(),
        'Wing Master Agent - Battambang',
        'master_agent',
        'ACTIVE',
        13.0957, 103.1984,
        'Street 1.5, Sangkat Svay Por',
        'Svay Por',
        'Battambang',
        'Battambang',
        '+855 53 952 952',
        '{"schedule": {"MONDAY": {"openTime": "08:00", "closeTime": "17:30"}, "TUESDAY": {"openTime": "08:00", "closeTime": "17:30"}, "WEDNESDAY": {"openTime": "08:00", "closeTime": "17:30"}, "THURSDAY": {"openTime": "08:00", "closeTime": "17:30"}, "FRIDAY": {"openTime": "08:00", "closeTime": "17:30"}}}',
        '["Cash In", "Cash Out", "Agent Network Management", "Float Management"]'
    ),
    (
        gen_random_uuid(),
        'Wing Bank Sihanoukville Branch',
        'branch',
        'ACTIVE',
        10.6271, 103.5289,
        'Ekareach Street',
        'Sangkat 3',
        'Prey Nob',
        'Preah Sihanouk',
        '+855 34 934 934',
        '{"schedule": {"MONDAY": {"openTime": "08:00", "closeTime": "17:00"}, "TUESDAY": {"openTime": "08:00", "closeTime": "17:00"}, "WEDNESDAY": {"openTime": "08:00", "closeTime": "17:00"}, "THURSDAY": {"openTime": "08:00", "closeTime": "17:00"}, "FRIDAY": {"openTime": "08:00", "closeTime": "17:00"}}}',
        '["Cash Deposit", "Cash Withdrawal", "Account Opening", "Loan Services"]'
    );
