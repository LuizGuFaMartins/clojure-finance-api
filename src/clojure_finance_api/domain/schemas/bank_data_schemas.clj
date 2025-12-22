(ns clojure-finance-api.domain.schemas.bank-data-schemas)

;; Schemas
(def BankDataCreateSchema
  [:map
   [:user-id uuid?]
   [:card-holder string?]
   [:card-last-4 [:re #"\d{4}"]]
   [:card-brand string?]
   [:expires-month [:int {:min 1 :max 12}]]
   [:expires-year [:int {:min 2024}]]])

(def BankDataIdSchema
  [:uuid])