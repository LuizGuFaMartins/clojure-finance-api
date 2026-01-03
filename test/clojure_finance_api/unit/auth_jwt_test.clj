(ns clojure-finance-api.unit.auth-jwt-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-finance-api.infra.auth.jwt :as jwt]
            [com.stuartsierra.component :as component]
            [clojure-finance-api.infra.components.auth-component :as auth-c]
            [clojure-finance-api.factories.user-factory :as user-fact]))

(deftest create-token-logic-test
  (testing "Deve gerar e validar um token com os claims corretos"
    (let [secret "my-test-secret"
          user (user-fact/build-user {:id (java.util.UUID/randomUUID) :role :admin})
          auth-comp (component/start (auth-c/new-auth-component {:auth {:jwt {:secret secret}}}))
          ctx {:components {:auth auth-comp}}]

      (try
        (let [token (jwt/create-token ctx user)]

          (testing "Geração do token"
            (is (string? token) "O token deve ser uma string")
            (is (not (clojure.string/blank? token)) "O token não deve ser vazio"))

          (testing "Decodificação e integridade dos dados (Claims)"
            (let [claims (jwt/verify-token ctx token)]
              (is (= (str (:id user)) (:id claims))
                  "O ID no token deve ser igual ao ID do usuário (como string)")
              (is (= "admin" (:role claims))
                  "O role no token deve ser 'admin'")
              (is (= "clojure-finance-api" (:aud claims))
                  "O campo audience (aud) deve estar correto")
              (is (= "access" (:type claims))
                  "O tipo do token deve ser 'access'")))

          (testing "Falha na validação com secret incorreto"
            (let [wrong-auth-comp (assoc auth-comp :secret "wrong-secret")
                  wrong-ctx       {:request {:components {:auth wrong-auth-comp}}}]
              (is (thrown? Exception
                           (jwt/verify-token wrong-ctx token))
                  "Deve lançar exceção ao tentar validar com contexto contendo segredo errado")))

           (testing "Token Expirado"
             (let [now (java.time.Instant/now)
                  past-payload {:id "123" :role "admin" :aud "clojure-finance-api" :type "access"
                                :exp (.getEpochSecond (.minus now 1 java.time.temporal.ChronoUnit/HOURS))}
                  expired-token (buddy.sign.jwt/sign past-payload secret {:alg :hs256})]
               (is (thrown? Exception (jwt/verify-token ctx expired-token))
                  "Deve lançar exceção para tokens expirados")))
          )

        (finally
          (component/stop auth-comp))))))