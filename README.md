# User Service — AWS Deployment Guide

Deploy a Dockerized Spring Boot + MySQL application on AWS EC2 using Terraform from AWS CloudShell.

---

## Architecture Overview

```
AWS CloudShell
    └── Terraform
            ├── Creates Security Group (ports 22, 8080)
            └── Creates EC2 (t2.micro)
                    └── user_data script runs on first boot
                            ├── Installs Docker + Docker Compose
                            ├── Clones GitHub repo
                            └── docker-compose up --build
                                    ├── mysql container (port 3306, internal only)
                                    └── user-service container (port 8080)
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Login and get JWT token |
| DELETE | `/api/users/{userId}` | Delete a user |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/api-docs` | OpenAPI JSON |

---

## Prerequisites

- AWS account with sandbox access
- GitHub account with this repo pushed (public repository)
- `devops-key.pem` downloaded to your local machine (created in Step 1)

---

## Step 1 — Create EC2 Key Pair

> Do this once. If you already have a key pair named `devops-key`, skip to Step 2.

1. Go to [AWS Console](https://console.aws.amazon.com)
2. Search for `EC2` in the top search bar → click EC2
3. In the left sidebar → **Network & Security** → **Key Pairs**
4. Click **Create key pair** (top right)
5. Fill in:
   - Name: `devops-key`
   - Key pair type: `RSA`
   - Private key file format: `.pem`
6. Click **Create key pair**
7. File `devops-key.pem` will auto-download — **save it, you cannot download it again**

---

## Step 2 — Open AWS CloudShell

1. In AWS Console top navigation bar, click the **terminal icon** (`>_`) next to the search bar
2. Wait ~30 seconds for CloudShell to initialize
3. You will see a prompt like:
   ```
   [cloudshell-user@ip ~]$
   ```

> **Sandbox note:** CloudShell resets between sessions but files in your home directory (`~`) persist within the same session. If your sandbox resets, repeat Steps 3–5.

---

## Step 3 — Install Terraform

Run each command one at a time:

```bash
sudo yum install -y yum-utils
```

```bash
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
```

```bash
sudo yum install -y terraform
```

```bash
terraform -version
```

Expected output: `Terraform v1.x.x` — confirms installation succeeded.

---

## Step 4 — Upload Your Key Pair to CloudShell

1. In CloudShell, click **Actions** (top right corner) → **Upload file**
2. Select `devops-key.pem` from your local machine
3. If you see `File already exists` error, the file is already there — skip this step

Fix permissions on the key file:

```bash
chmod 400 ~/devops-key.pem
```

---

## Step 5 — Create Terraform Project

```bash
mkdir -p ~/casestudy/ && cd ~/casestudy/
```

### Create `variables.tf`

```bash
cat > variables.tf << 'EOF'
variable "aws_region" {
  default = "us-east-1"
}

variable "key_name" {
  default = "devops-key"
}

variable "github_repo" {
  default = "https://github.com/MonarchAP18/case-study.git"
}

variable "MYSQL_ROOT_PASSWORD" {
  sensitive = true
}

variable "JWT_SECRET" {
  sensitive = true
}
EOF
```

> Replace `<your-username>` with your actual GitHub username.

### Create `main.tf`

```bash
cat > main.tf << 'EOF'
provider "aws" {
  region = var.aws_region
}

resource "aws_security_group" "user_service_sg" {
  name        = "user-service-sg"
  description = "Allow SSH and app traffic"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "user_service_ec2" {
  ami                    = "ami-0c02fb55956c7d316"
  instance_type          = "t2.micro"
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.user_service_sg.id]

  user_data = <<-SCRIPT
    #!/bin/bash
    yum update -y
    yum install -y git

    amazon-linux-extras install docker -y
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ec2-user

    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-linux-x86_64" \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    ln -s /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose

    cd /home/ec2-user
    git clone ${var.github_repo} app
    cd app

    export MYSQL_ROOT_PASSWORD="${var.MYSQL_ROOT_PASSWORD}"
    export JWT_SECRET="${var.JWT_SECRET}"

    /usr/local/bin/docker-compose up -d --build
  SCRIPT

  tags = {
    Name = "user-service-ec2"
  }
}
EOF
```

### Create `outputs.tf`

```bash
cat > outputs.tf << 'EOF'
output "ec2_public_ip" {
  value = aws_instance.user_service_ec2.public_ip
}

output "app_url" {
  value = "http://${aws_instance.user_service_ec2.public_ip}:8080"
}
EOF
```

### Verify all 3 files exist

```bash
ls -la
```

Expected: `main.tf`, `variables.tf`, `outputs.tf`

---

## Step 6 — Deploy with Terraform

> Choose your own values for `mysql_root_password` and `jwt_secret`. Write them down — you need the same values for destroy later.
> - `mysql_root_password` — min 8 chars, mix letters + numbers + special char (e.g., `Admin1234!`)
> - `jwt_secret` — must be at least 32 characters long

```bash
terraform init
```

```bash
terraform plan \
  -var="MYSQL_ROOT_PASSWORD=root" \
  -var="JWT_SECRET=thisIsMySecretKeyForJwtSigning9876"
```

Review the plan — it should show 2 resources to be created: security group + EC2 instance.

```bash
terraform apply \
  -var="MYSQL_ROOT_PASSWORD=root" \
  -var="JWT_SECRET=thisIsMySecretKeyForJwtSigning9876"
```

- Type `yes` when prompted
- Wait ~1 minute
- At the end you will see:
  ```
  ec2_public_ip = "xx.xx.xx.xx"
  app_url = "http://xx.xx.xx.xx:8080"
  ```
- **Copy the public IP** — you will need it

---

## Step 7 — Monitor EC2 Startup

SSH into the EC2 instance:

```bash
ssh -i ~/devops-key.pem ec2-user@<EC2_PUBLIC_IP>
```

Watch the startup log:

```bash
sudo tail -f /var/log/cloud-init-output.log
```

Wait until you see docker-compose finishing. Press `Ctrl+C` to stop.

Check both containers are running:

```bash
sudo docker ps
```

Expected output — two containers running:

```
CONTAINER ID   IMAGE              STATUS
xxxxxxxxxxxx   app-user-service   Up X minutes
xxxxxxxxxxxx   mysql:8.0          Up X minutes (healthy)
```

Exit EC2:

```bash
exit
```

---

## Step 8 — Test the Application

> Wait 4–5 minutes after `terraform apply` before testing. The Docker build takes time.

### Register a user

```bash
curl -X POST http://<EC2_PUBLIC_IP>:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"testuser","email":"test@example.com","password":"pass1234","role":"CUSTOMER"}'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "role": "USER"
}
```

### Login

```bash
curl -X POST http://<EC2_PUBLIC_IP>:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass1234"}'
```

### Swagger UI

Open in your browser:

```
http://<EC2_PUBLIC_IP>:8080/swagger-ui.html
```

---

## Troubleshooting

### Containers not running after 5 minutes

SSH into EC2 and check logs:

```bash
ssh -i ~/devops-key.pem ec2-user@<EC2_PUBLIC_IP>
sudo cat /var/log/cloud-init-output.log
sudo docker ps -a
sudo docker logs user-service
```

### Security group already exists error

If you destroyed and re-applied and get a `user-service-sg already exists` error:

```bash
terraform destroy \
  -var="MYSQL_ROOT_PASSWORD=Admin1234!" \
  -var="JWT_SECRET=thisIsMySecretKeyForJwtSigning9876"
```

Wait 2 minutes, then apply again.

### Sandbox session reset — Terraform state lost

If your sandbox resets and you lost the Terraform state but EC2 is still running:

1. Go to AWS Console → EC2 → terminate the instance manually
2. Go to AWS Console → EC2 → Security Groups → delete `user-service-sg`
3. Re-run from Step 5

---

## Step 9 — Cleanup (Avoid Charges)

Always destroy resources when done:

```bash
cd ~/casestudy/terraform-project

terraform destroy \
  -var="MYSQL_ROOT_PASSWORD=Admin1234!" \
  -var="JWT_SECRET=thisIsMySecretKeyForJwtSigning9876"
```

Type `yes` — this deletes the EC2 instance and security group.

Verify in AWS Console → EC2 → Instances — the instance should show `terminated`.

---

## Project File Structure

```
user-service/                          # GitHub repo root
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/user/
│   │   │   ├── config/
│   │   │   │   ├── JwtUtil.java       # JWT token generation (secret from env)
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   └── UserController.java  # /register, /login, /{userId}
│   │   │   ├── service/UserService.java
│   │   │   ├── repository/UserRepository.java
│   │   │   ├── entity/User.java
│   │   │   └── dto/
│   │   └── resources/
│   │       └── application.properties  # All config via env vars
│   └── test/
├── Dockerfile                          # Multi-stage build (Maven + JRE)
├── docker-compose.yml                  # mysql + user-service containers
├── pom.xml
└── .gitignore                          # Excludes target/, *.zip

terraform-project/                     # Lives only in CloudShell
├── main.tf                            # EC2 + Security Group
├── variables.tf                       # Input variables
└── outputs.tf                         # Prints IP and URL after apply
```

---

## Environment Variables Reference

| Variable | Used By | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | MySQL + Spring Boot | Database root password |
| `JWT_SECRET` | Spring Boot | Secret key for signing JWT tokens (min 32 chars) |
| `SPRING_DATASOURCE_URL` | Spring Boot | Set automatically in docker-compose |
| `EUREKA_ENABLED` | Spring Boot | Set to `false` — no Eureka server in this setup |
