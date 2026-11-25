# Online Voting System

A Java Servlets/JSP/MySQL online voting project with separate voter and Election Commission interfaces.

## Features

- Voter registration and login
- Mandatory voter-card ID plus Aadhaar/PAN identity
- Minimum 16-character strong passwords with an in-page generator
- PBKDF2-SHA256 password hashing
- Per-session CSRF tokens
- Login failure lockout
- One vote per voter
- Anonymous candidate choice in the ballot table
- Transaction-safe duplicate-vote prevention
- Election open/close controls
- Live Commissioner totals
- Final winner declaration
- Excel-compatible CSV export
- Separate voter and Commission UI
- Security headers

## Security status

This project is suitable for a local college demonstration. It is **not** approved or secure enough for a real government election.

Do not treat format-based voter ID/PAN checks or the demo DigiLocker configuration as authoritative identity verification. Real deployment requires an approved identity provider and independent election security review.

## Requirements

- JDK 17 or newer
- Apache Tomcat 10.1
- MySQL 8 or compatible MariaDB
- Maven

The commands below are for Ubuntu/Debian. Package installation and initial
database provisioning need a machine administrator, but the application uses a
separate, restricted MySQL account. Do not configure the application to connect
as MySQL `root`. If you do not have administrator access, ask the administrator
to install/start the services and run the one-time database setup below.

### 1. Install and start the software

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven mysql-server tomcat10
sudo systemctl enable --now mysql tomcat10
```

Check that Java, Maven, and MySQL are available:

```bash
java -version
mvn -version
sudo systemctl status mysql --no-pager
```

If you already have MySQL, Tomcat 10.1, and JDK 17 installed, skip the package
installation. MySQL and Tomcat must be running before you launch the application.

### 2. Create the database and restricted application user

The schema creates the `voting_system` database and its tables. Run it once
using a MySQL administrator account. On Ubuntu's default socket-authenticated
installation, use `sudo mysql`. If you have a password-authenticated MySQL
administrator account, use that account instead. A DBA can run these commands
for you if you do not have administrator access.

```bash
sudo mysql < database/schema.sql
sudo mysql
```

For a password-authenticated MySQL administrator, replace `sudo mysql` with
`mysql -u YOUR_DB_ADMIN -p` (and append `< database/schema.sql` for the first
command). `YOUR_DB_ADMIN` can be any account with database/user provisioning
privileges; it does not have to be named `root`.

At the MySQL prompt, create a dedicated account. Replace the sample password
with a strong, unique password and keep it for the runtime configuration:

```sql
CREATE USER 'voter_app'@'127.0.0.1' IDENTIFIED BY 'replace-with-a-strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON voting_system.* TO 'voter_app'@'127.0.0.1';
EXIT;
```

The app account has only data access; it cannot create databases or users. The
`DELETE` permission is needed to clear successful login-attempt records.

Load the fictional local demonstration accounts, identities, and candidates
using the new restricted account:

```bash
mysql -h 127.0.0.1 -u voter_app -p < database/demo-data.sql
```

Enter the application user's password when prompted. Verify the connection:

```bash
mysql -h 127.0.0.1 -u voter_app -p voting_system \
  -e "SELECT COUNT(*) AS candidates FROM candidates;"
```

It should report three candidates. The `demo-data.sql` file contains fictional
identities and credentials only. Never load it, real personal data, or
production credentials into a public deployment.
If you already loaded an older version of the demo data and its voter login
does not work, rerun the same import command to refresh the documented demo
passwords. This also closes the election, but does not clear existing ballots
or reset whether a voter has voted.

### 3. Configure Tomcat to connect as the app user

The Tomcat service must receive these environment variables. Store the database
password in a root-readable-only environment file rather than in source code:

```bash
sudo touch /etc/voter-system.env
sudo chown root:root /etc/voter-system.env
sudo chmod 600 /etc/voter-system.env
sudoedit /etc/voter-system.env
```

Put the following in the file, using the same password as in the `CREATE USER`
statement:

```text
DB_URL=jdbc:mysql://127.0.0.1:3306/voting_system
DB_USER=voter_app
DB_PASSWORD=replace-with-a-strong-password
IDENTITY_VERIFICATION_MODE=DEMO
```

Connect that file to Tomcat's systemd service:

```bash
sudo systemctl edit tomcat10
```

Add these lines in the editor and save:

```ini
[Service]
EnvironmentFile=/etc/voter-system.env
```

Then reload the service configuration:

```bash
sudo systemctl daemon-reload
```

`DB_URL`, `DB_USER`, and `DB_PASSWORD` are read by the application at startup.
The default identity mode is also the local demo registry; any value other than
`DIGILOCKER` uses that demo registry. Registration must match one of the
synthetic identities below. This is not real identity or document ownership
verification.

For DigiLocker API Setu integration, use `IDENTITY_VERIFICATION_MODE=DIGILOCKER`
only after receiving approved provider credentials and configuring the OAuth
callback over HTTPS. Configure these variables in the same private environment
file:

```text
DIGILOCKER_CLIENT_ID=your-client-id
DIGILOCKER_CLIENT_SECRET=your-client-secret
DIGILOCKER_AUTH_URL=https://provider.example/authorize
DIGILOCKER_TOKEN_URL=https://provider.example/token
DIGILOCKER_USER_URL=https://provider.example/user
DIGILOCKER_REDIRECT_URI=https://your-domain/voter-system/digilocker/callback
DIGILOCKER_SCOPE=your-approved-scope
```

The redirect URI must match the registered API callback:

```text
https://your-domain/voter-system/digilocker/callback
```

Use HTTPS for the deployed application. A reverse proxy or HTTPS-enabled Tomcat is required before setting secure cookies.

### 4. Build and deploy

Build the WAR from the project directory:

```bash
mvn clean package
```

Deploy it to the Tomcat 10 package's web application directory and restart
Tomcat:

```text
sudo cp target/voter-system.war /var/lib/tomcat10/webapps/
sudo systemctl restart tomcat10
```

Wait for Tomcat to finish deploying the WAR, then open the app at:

```text
http://localhost:8080/voter-system/
```

Tomcat's default HTTP port is `8080`. If your Tomcat is configured for another
port or has a different deployment directory, use that port/path instead.

## Run a voter and Election Commission smoke test

The initial election is closed. Use these included demo accounts for a local
smoke test:

| Role | Email | Password |
|---|---|---|
| Voter | `demo.voter@example.test` | `DemoStudent!2026Strong` |
| Voter | `demo.user@example.test` | `TestUser!2026Strong` |
| Election Commission | `demo.commission@example.test` | `DemoCommission!2026Strong` |

1. Open `http://localhost:8080/voter-system/commission/login.jsp` and sign in
  with the Election Commission demo account.
2. Select a voting end time in the future and choose **Reopen election**.
  The dashboard should show that voting is live.
3. Open `http://localhost:8080/voter-system/` in another browser window, sign
  in with a voter demo account, select a candidate, and submit the ballot.
  The voter dashboard should confirm the ballot was recorded.
4. Try to vote again with that account. A second ballot must not be accepted.
5. Return to the Commission dashboard. The total ballot count and candidate
  totals should have increased. Choose **Close voting and declare winner** to
  close the election and display the result.
6. Sign out and confirm that the voter and Commission dashboards require login.

To test registration instead, open
`http://localhost:8080/voter-system/register.jsp` and use one row from the
synthetic identity table below. Enter that row's exact name and voter-card ID,
then select either its PAN or Aadhaar number. Supply a unique email and a
password of at least 16 characters containing uppercase and lowercase letters,
a number, and a symbol. Registration with mismatched or unlisted IDs must be
rejected.

Other useful checks: repeat registration with the same email or voter ID (it
must fail), try five incorrect logins (the account/IP combination is locked for
15 minutes), and download the Commission CSV report (it contains candidate
totals, not voter identity). Do not use real identity data for testing.

## Prepare and record a local demo video

The current development checkout has a project-local Tomcat under `.runtime`
configured for port `7690`. This is separate from the system Tomcat setup above,
which normally uses port `8080`. The `.runtime` folder is machine-specific and
may contain database credentials: do not put it in a ZIP you share.

From the project directory, make sure MariaDB/MySQL is running. Set up the
schema only if this is a new database:

```bash
sudo mysql < database/schema.sql
```

The demo registry was cleared after testing. Reload its fictional records and
demo accounts before recording (enter the app user's password when prompted):

```bash
mysql -h 127.0.0.1 -u voter_app -p < database/demo-data.sql
```

This seed closes voting but does not delete ballots or reset existing voters'
`has_voted` status. Use an unused demo identity for the recording.

Build, deploy, and restart the project-local Tomcat:

```bash
mvn clean package
cp target/voter-system.war .runtime/tomcat/webapps/voter-system.war
./.runtime/tomcat/bin/shutdown.sh
./.runtime/tomcat/bin/startup.sh
```

If Tomcat was already stopped, skip `shutdown.sh`. Wait for startup, then open
`http://localhost:7690/voter-system/`. For a different computer without
`.runtime`, use the system Tomcat deployment instructions above instead.

### Use this database from another local project folder

If you copied this same voter-system project to another folder on this
computer, build the WAR from that copy and configure the Tomcat instance that
will run it with the same database settings:

```text
DB_URL=jdbc:mysql://127.0.0.1:3306/voting_system
DB_USER=voter_app
DB_PASSWORD=the-existing-voter_app-password
IDENTITY_VERIFICATION_MODE=DEMO
```

Do not create a second database or run the schema again if `voting_system`
already exists. The new Tomcat must have access to the existing MySQL account.
If you need to demonstrate registration after the synthetic registry was
cleared, reload `database/demo-data.sql` using the app account as described
above. This closes the election but preserves existing ballots and voter
`has_voted` flags.

If the other project is on a different computer, `127.0.0.1` will point to
that computer, not this one. Use the MySQL server's private network address
only after its administrator enables restricted network access; do not expose
MySQL to the public internet.

### Suggested recording sequence

Use OBS Studio or your desktop's screen recorder. Show only fictional demo
data; do not show database passwords or environment files.

1. Open `http://localhost:7690/voter-system/register.jsp`.
2. Register **Demo Voter 3** with voter-card ID `VTR1000003` and Aadhaar
   `470332676732`. Use a new email and a strong demo password.
3. Sign in as the Election Commission at
   `http://localhost:7690/voter-system/commission/login.jsp` using
   `demo.commission@example.test` / `DemoCommission!2026Strong`, and open the
   election with a future end time.
4. Sign in as the newly registered voter, cast one ballot, and show the
   confirmation.
5. Return to the Commission dashboard to show the updated totals, then close
   voting and show the declared result.

These are synthetic records for a classroom demonstration only. The demo
registry does not verify real identity or document ownership.

### Make a source ZIP to share

From the Desktop directory, create a source-only archive. It excludes the
machine-local Tomcat configuration and generated build output:

```bash
cd ~/Desktop
zip -r voter-system-source.zip voter-system \
  -x 'voter-system/.runtime/*' 'voter-system/target/*' 'voter-system/build/*'
```

The recipient needs JDK 17+, Maven, MySQL/MariaDB, and Tomcat, then follows this
README to configure their own database credentials and deploy the app. Do not
include `.runtime`, database dumps, or private credentials in a shared ZIP.

## Demo Registration Limitation
This project uses a local demonstration database, not real Aadhaar, PAN, voter, or DigiLocker verification.

For registration in demo mode, the entered name, voter-card ID, and selected
PAN or Aadhaar number must all belong to the same pre-approved synthetic record
in `approved_voters`.

### Approved synthetic identities

| Name | Voter card ID | PAN | Aadhaar |
|---|---|---|---|
| Demo Voter 1 | `VTR1000001` | `ABCDE1234F` | `250082403651` |
| Demo Voter 2 | `VTR1000002` | `ADBIU2364F` | `882216630950` |
| Demo Voter 3 | `VTR1000003` | `BCDEF2345G` | `470332676732` |
| Demo Voter 4 | `VTR1000004` | `CDEFG3456H` | `348730152557` |
| Demo Voter 5 | `VTR1000005` | `DEFGH4567J` | `215058338127` |

The name must match the selected PAN/Aadhaar registry row and the voter-card
ID registry row. The sample Aadhaar values pass the application's local
checksum check; they are fictional test data, not real identity numbers.

### Synthetic demo accounts

The following accounts are created by `database/demo-data.sql`:

| Role | Email | Password |
|---|---|---|
| Voter | `demo.voter@example.test` | `DemoStudent!2026Strong` |
| Voter | `demo.user@example.test` | `TestUser!2026Strong` |
| Election Commission | `demo.commission@example.test` | `DemoCommission!2026Strong` |

These identities and credentials are fictional. They do not represent real
citizens and do not prove ownership of an identity document.

The application therefore demonstrates format validation and registry matching only. Real Aadhaar/PAN/voter ownership verification requires authorized provider credentials, institutional approval, HTTPS, and a production identity-verification integration. It is intentionally not implemented in this local college-demo build.



