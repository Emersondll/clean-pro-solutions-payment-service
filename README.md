# Clean Pro Solutions - Payment Service 💰

## 🎯 Papel no Ecossistema
O **Payment Service** processa todas as transações financeiras da plataforma. Ele lida com:
- Processamento de pagamentos vinculados a agendamentos.
- Gestão de status financeiro (PAID, REFUNDED, FAILED).
- Emissão de recibos e histórico de transações.
- Integração com gateways de pagamento (simulado/extensível).

## 🚀 Tecnologias
- **Java 21** & **Spring Boot 3.3.4**
- **MongoDB** (Persistência de transações)
- **RabbitMQ** (Processamento assíncrono de pagamentos via eventos)
- **Netflix Eureka** (Service Discovery)

## 🛠️ Como Executar

### 1. Execução Isolada (Individual)
Para rodar este serviço e suas dependências:
```bash
docker-compose up -d --build
```
O serviço estará disponível em `http://localhost:8087`.

### 2. Execução Integrada
Este serviço é orquestrado pelo projeto principal [Clean Pro Platform](../README.md).

---
© 2026 Clean Pro Solutions - Desenvolvido por Emerson Lima.
