-- DEMO ONLY: all names, email addresses, voter IDs, Aadhaar numbers, and PAN
-- numbers below are synthetic and must never be used for a real election.
-- No Aadhaar/PAN ownership is verified by this project.
USE voting_system;

-- Demo voter card, PAN, and Aadhaar values are paired by exact demo name.
INSERT INTO approved_voters (document_type, document_number, full_name)
VALUES
    -- At least 5 PAN numbers
    ('PAN', 'ABCDE1234F', 'Demo Voter 1'),
    ('PAN', 'ADBIU2364F', 'Demo Voter 2'),
    ('PAN', 'BCDEF2345G', 'Demo Voter 3'),
    ('PAN', 'CDEFG3456H', 'Demo Voter 4'),
    ('PAN', 'DEFGH4567J', 'Demo Voter 5'),

    -- At least 5 Aadhaar numbers (12 digits)
    ('AADHAAR', '250082403651', 'Demo Voter 1'),
    ('AADHAAR', '882216630950', 'Demo Voter 2'),
    ('AADHAAR', '470332676732', 'Demo Voter 3'),
    ('AADHAAR', '348730152557', 'Demo Voter 4'),
    ('AADHAAR', '215058338127', 'Demo Voter 5'),

    -- At least 5 Voter ID numbers
    ('VOTER_ID', 'VTR1000001', 'Demo Voter 1'),
    ('VOTER_ID', 'VTR1000002', 'Demo Voter 2'),
    ('VOTER_ID', 'VTR1000003', 'Demo Voter 3'),
    ('VOTER_ID', 'VTR1000004', 'Demo Voter 4'),
    ('VOTER_ID', 'VTR1000005', 'Demo Voter 5')
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name);

-- DEMO LOGIN CREDENTIALS
-- Demo Student: demo.voter@example.test / DemoStudent!2026Strong
-- Test User: demo.user@example.test / TestUser!2026Strong
-- Election Commission: demo.commission@example.test / DemoCommission!2026Strong
INSERT IGNORE INTO candidates (id, name, party, vote_count)
VALUES
    (101, 'Aarav Sharma', 'Student Progress Group', 0),
    (102, 'Meera Patel', 'Campus Development Group', 0),
    (103, 'Kabir Khan', 'Independent Student Voice', 0);

INSERT IGNORE INTO users
    (name, email, voter_id, identity_number, password, role, has_voted)
VALUES
    (
        'Demo Student',
        'demo.voter@example.test',
        'DEMO-VOTER-001',
        'ABCDE1234F',
        'pbkdf2$120000$FkJu6X5mXEHMoSCgiEqX3Q==$YWC5p+8XXuPS1nZPEycFIkbIMB60fdbQ+SN9KPpwSsE=',
        'voter',
        FALSE
    ),
    (
        'Test User',
        'demo.user@example.test',
        'DEMO-VOTER-002',
        'ADBIU2364F',
        'pbkdf2$120000$HWvlGnA0lsV4ozm+xuNNaQ==$LvuvfvoumA6F/mxwtupQYPXsBfo8lY1msV1QzpHaKEQ=',
        'voter',
        FALSE
    ),
    (
        'Demo Election Commission',
        'demo.commission@example.test',
        'DEMO-COMMISSION-001',
        'DEMO-COMMISSION-ID',
        'pbkdf2$120000$RIhesC6Qvj2qFgtnrQbyow==$CCx7IMGPY8SXu5g3vief8QUqYQpVxBEwyM4yzE1VRP4=',
        'commission',
        FALSE
    );

UPDATE election_settings
SET status = 'CLOSED', end_time = NULL
WHERE id = 1;