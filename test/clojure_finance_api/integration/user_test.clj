(ns clojure-finance-api.integration.user-test
  (:require [clojure.test :refer [deftest is testing]]
            [clj-http.client :as client]
            [com.stuartsierra.component :as component]
            [clojure-finance-api.integration.containers.postgres :as pg]
            [clojure-finance-api.integration.system :as ts]
            [clojure-finance-api.factories.user-factory :as user-fact]
            [clojure-finance-api.infra.auth.hash :as hash-util]
            [next.jdbc :as jdbc]))

(defmacro with-system [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try ~@body (finally (component/stop ~bound-var)))))

(deftest user-creation-test
  (testing "Fluxo de criação de usuário com Docker 29.x compatível"
    (let [db-container (pg/create-pg-container)]
      (try
        (with-system [sut (ts/test-system (pg/get-db-config db-container))]
           (let [ds (get-in sut [:datasource :datasource])
                 base-url (partial ts/get-url sut)

                 ;; Preparando dados
                 admin (user-fact/build-user {:role :admin :password "admin123"})
                 new-user (user-fact/build-user {:role :user})]

             ;; Seed do Admin
             (jdbc/execute! ds ["INSERT INTO users (id, email, password, role, name) VALUES (?, ?, ?, ?, ?)"
                                (:id admin) (:email admin)
                                (hash-util/hash-password "admin123")
                                "admin" (:name admin)])

             ;; Teste de Login e Criação
             (let [login-resp (client/post (base-url "/login")
                                           {:json-params {:email (:email admin) :password "admin123"}
                                            :content-type :json :as :json})
                   token (get-in login-resp [:body :token])
                   headers {"Authorization" (str "Bearer " token)}]

               (is (= 200 (:status login-resp)))

               (let [create-resp (client/post (base-url "/users")
                                              {:headers headers
                                               :json-params new-user
                                               :content-type :json :as :json})]
                 (is (= 201 (:status create-resp)))
                 (is (= (:email new-user) (get-in create-resp [:body :email])))))))
        (finally
          (pg/stop-container! db-container))))))