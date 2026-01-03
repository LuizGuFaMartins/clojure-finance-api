(ns clojure-finance-api.unit.hash-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-finance-api.infra.auth.hash :as hash-util]))

(deftest password-hashing-test
  (let [password "senha-secreta-123"
        hashed   (hash-util/hash-password password)]

    (testing "Geração de Hash"
      (is (string? hashed) "O hash deve ser uma string")
      (is (not= password hashed) "O hash nunca deve ser igual à senha pura")
      (is (not (clojure.string/blank? hashed)) "O hash não deve ser vazio"))

    (testing "Verificação de Senha Correta"
      (is (true? (hash-util/check-password password hashed))
          "Deve retornar true para a senha correta"))

    (testing "Verificação de Senha Incorreta"
      (is (false? (hash-util/check-password "senha-errada" hashed))
          "Deve retornar false para uma senha que não bate"))

    (testing "Segurança (Salt)"
      (let [hashed-2 (hash-util/hash-password password)]
        (is (not= hashed hashed-2)
            "Duas execuções com a mesma senha devem gerar hashes diferentes (uso de salt)")))))