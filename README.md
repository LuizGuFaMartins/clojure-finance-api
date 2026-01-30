Aqui está uma proposta de **README.md** profissional e focado na sua arquitetura de camadas separadas, incluindo o guia para criação do usuário administrativo.

---

# Finance API 🛡️

API para estudo da linguagem clojure e as principais implementações de segurança, utilizando o servidor **Pedestal**, componentes **Stuart Sierra** e persistência com **Next.JDBC** + **PostgreSQL**.

## 🏗️ Arquitetura de Dados

A API utiliza **Row Level Security (RLS)** no PostgreSQL para isolamento de dados. Por segurança, a aplicação se conecta utilizando um usuário de runtime (`finance_app_user`) com permissões restritas.

---

## 🚀 Como Executar

### 1. Preparar a Infraestrutura (Banco de Dados)

O banco de dados deve ser iniciado antes da aplicação. Ele contém os scripts de inicialização que configuram os usuários e permissões de banco.

```bash
# Sobe o container do Postgres e executa o init.db
docker compose -f docker-compose.db.yml up -d

```

### 2. Configurar o Ambiente

Certifique-se de que o seu arquivo `.env` na raiz do projeto está preenchido:

```env
# --- Database (Postgres) ---
POSTGRES_DB=
POSTGRES_HOST=
POSTGRES_PORT=

# Usado pela aplicação para se conectar
POSTGRES_USER=
POSTGRES_PASSWORD=

# Usado pelo Docker Compose para subir o container do banco
ADMIN_POSTGRES_USER=
ADMIN_POSTGRES_PASSWORD=
```

### 3. Rodar a Aplicação

Com o banco ativo e as variáveis configuradas:

**Via Terminal:**

```bash
clojure -M:run
```

**Via Docker (Build de produção):**

```bash
docker compose up --build
```

---

## 🔑 Guia: Criando um Usuário Admin Manualmente

Para o primeiro acesso, você precisará gerar um usuário manualmente. Como a API utiliza o algoritmo `:bcrypt+sha512` com 12 iterações via **Buddy**, o hash deve ser gerado no REPL para garantir compatibilidade.

### Passo 1: Gerar o UUID

Abra o REPL do projeto e execute:

```clojure
(require '[buddy.hashers :as hs])

;; Gere o UUID
(java.util.UUID/randomUUID)

;; O resultado será algo como: #uuid "3aac6b39-3c4f-4b5b-8ab6-6728720be4ae"
```

### Passo 2: Gerar o Hash da Senha

Abra o REPL do projeto e execute:

```clojure
(require '[buddy.hashers :as hs])

;; Gere o hash com as configurações da API
(hs/derive "SuaSenhaForte123" {:alg :bcrypt+sha512 :iterations 12})

;; O resultado será algo como: "bcrypt+sha512$2a$12$L7...base64..."
```

### Passo 3: Inserir no Banco de Dados

Com o hash e uuid em mãos, conecte-se ao seu Postgres e execute o SQL abaixo.

> **Nota:** É crucial adicionar a role `admin` no campo `user_roles` (formato array do Postgres) para que o usuário tenha privilégios de acesso.

```sql
-- 1. Inserir o usuário e capturar o ID
INSERT INTO users (id, name, email, password, cpf, phone, active)
VALUES (
           'COLE_AQUI_O_UUID_DO_REPL',
           'Administrador',
           'admin@email.com',
           'COLE_AQUI_O_HASH_DO_REPL',
           '12345678901',
           '11999999999',
           true
       );

-- 2. Vincular à Role de Admin
-- Buscamos o ID da role 'admin' que o Flyway criou
INSERT INTO user_roles (user_id, role_id)
VALUES (
           'COLE_AQUI_O_UUID_DO_REPL',
           (SELECT id FROM roles WHERE name = 'admin' LIMIT 1)
    );
```