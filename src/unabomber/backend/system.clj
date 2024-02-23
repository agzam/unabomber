(ns unabomber.backend.system
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.exception]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.adapter.jetty :as jetty]
            [unabomber.backend.giantbomb :as giantbomb]
            [unabomber.backend.index-page :refer [index-page]]
            [unabomber.backend.secrets :as secrets])
  (:import [org.eclipse.jetty.server Server])
  (:gen-class))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get {:handler (fn [_]
                            {:status 200
                             :body (index-page)})}}]
     ["/search" {:get giantbomb/handler}]

     ["/status"
      {:get {:handler (constantly
                       {:status 200
                        :body {:status :okay}})}}]]
    {:conflicts (fn [conflicts]
                  (log/error
                   (reitit.exception/format-exception :path-confilcts nil conflicts)))
     :data {:muuntaja m/instance
            :middleware [;; swagger feature
                         ;; swagger/swagger-feature
                         ;; query-params & form-params
                         parameters/parameters-middleware
                         ;; content-negotiation
                         muuntaja/format-negotiate-middleware
                         ;; encoding response body
                         muuntaja/format-response-middleware
                         ;; exception handling
                         (exception/create-exception-middleware
                          {::exception/default
                           (partial
                            exception/wrap-log-to-console
                            exception/default-handler)})
                         ;; decoding request body
                         muuntaja/format-request-middleware
                         ;; coercing response bodys
                         coercion/coerce-response-middleware
                         ;; coercing request parameters
                         coercion/coerce-request-middleware
                         ;; multipart
                         ;; multipart/multipart-middleware
                         ]}})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404
                              :header {"Content-Type" "text/html"}
                              :body "Page Not Found"})}))))

(defmethod ig/init-key ::server [_ opts]
  (log/info "jetty server started on port: " (:port opts))
  (jetty/run-jetty app opts))

(defmethod ig/halt-key! ::server [_ ^Server server]
  (log/info "shutting down the server")
  (.stop server))

(defn usage []
  (->> [""
        "Options:"
        " --help        -- este ayuda"
        " --port PORT   -- server port"
        " --api-key KEY -- Giantbomb API key"]
       (str/join \newline)))

(def ^:private cli-options
  [
   #_["-c" "--config FILE" "configuration file"
      :default "dev-resources/config.edn"
      :id :config]
   ["-p" "--port PORT" "server port" :id :port]
   ["-a" "--api-key KEY" "giantbomb api key" :id :api-key]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [port help api-key]} (some-> (cli/parse-opts args cli-options)
                                            :options)
        port (-> port edn/read-string (or 3000))]
    (when help
      (println (usage))
      (System/exit 0))
    (when api-key
      (log/info "giantbomb api is set via parameter")
      (alter-var-root #'secrets/*api-key* (constantly api-key)))
    (jetty/run-jetty app {:port port
                          :join? false})
    (log/info (str "running the server on port: " port))))
