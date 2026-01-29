(ns clojure-finance-api.infra.graphql.core
  (:require [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [clojure-finance-api.infra.security.jwt :as jwt]
            [clojure-finance-api.infra.graphql.resolvers :as res]
            [clojure-finance-api.infra.graphql.schemas.types :as types]
            [clojure-finance-api.infra.graphql.schemas.queries :as queries]
            [clojure-finance-api.infra.graphql.schemas.mutations :as mutations]
            [clojure-finance-api.infra.graphql.schemas.input-types :as inputs]))

(defn- build-schema []
  {:objects (merge types/transaction-types)
   :queries (merge queries/transaction-queries)
   :mutations (merge mutations/transaction-mutations)
   :input-objects (merge inputs/input-types)})

(defn compiled-schema []
  (-> (build-schema)
      (attach-resolvers {:query/all-transactions (jwt/authorization-resolver [:admin] res/all-transactions)
                         :query/my-transactions (jwt/authorization-resolver [:customer] res/my-transactions)
                         :mutation/create-transaction (jwt/authorization-resolver [:customer]res/create-transaction)})
      (schema/compile {:enable-introspection? false})))