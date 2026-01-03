(ns clojure-finance-api.factories.user-factory
  (:require [clojure.test :refer :all]))

(defn build-user
  "Cria um mapa de usuário para testes."
  ([] (build-user {}))
  ([overrides]
   (merge {:id (java.util.UUID/randomUUID)
           :name "Test User"
           :email (str "test-" (random-uuid) "@finance.com")
           :password "password123"
           :role :admin}
          overrides)))