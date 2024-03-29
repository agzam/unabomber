(ns unabomber.backend.giantbomb
  (:require
   [unabomber.backend.secrets :as secrets]
   [clj-http.client :as client]))

(defn handler [request]
  (let [term (get-in request [:query-params "term"])
        api-key (or secrets/*api-key*
                    (secrets/decrypt-api-key))
        search-url
        (format
         "https://www.giantbomb.com/api/search?api_key=%s&format=json&query=%s&resources=game"
         api-key term)
        res (client/get
             search-url
             {:as :json
              :headers {:user-agent "unabomber"}})]

    {:status 200
     :body (some->> res :body :results)}))
