# clean-pro-solutions-payment-service

Processamento de pagamentos e integraÃ§Ã£o com gateways (Webhooks).

## ðŸš€ Tecnologias
- **Java 21**
- **Spring Boot 3.3.4**
- **Spring Cloud (Eureka, OpenFeign, Config)**
- **MongoDB** (PersistÃªncia de dados)
- **RabbitMQ** (Mensageria assÃ­ncrona)
- **JaCoCo** (RelatÃ³rios de cobertura)
- **SpringDoc OpenAPI** (DocumentaÃ§Ã£o Swagger)

## ðŸ“Š Qualidade e Testes
Este projeto possui uma regra de integridade de cÃ³digo rigorosa:
- **Cobertura MÃ­nima**: 80% de instruÃ§Ãµes cobertas (MandatÃ³rio).
- **Enforcement**: O build falha automaticamente via plugin JaCoCo na fase de erify caso a cobertura seja inferior ao limite.

## ðŸ› ï¸ Como rodar

### PrÃ©-requisitos
- Docker e Docker Compose instalados.
- JDK 21 instalado localmente (opcional se usar Docker).

### Via Maven (Local)
`ash
mvn clean verify
`

### Via Docker
`ash
docker build -t clean-pro-solutions-payment-service .
`

## ðŸ—ï¸ Arquitetura
O serviÃ§o segue os princÃ­pios de **Clean Architecture** e **Domain-Driven Design (DDD)**, com as seguintes camadas:
- **Controller**: Porta de entrada para requisiÃ§Ãµes REST.
- **Service**: Regras de negÃ³cio e orquestraÃ§Ã£o.
- **Repository**: PersistÃªncia desacoplada via Spring Data.
- **Document/Entity**: Modelagem do domÃ­nio.

---
Â© 2026 Clean Pro Solutions - Todos os direitos reservados.
