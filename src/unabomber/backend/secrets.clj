(ns unabomber.backend.secrets
  (:require
   [clojure.edn :as edn]
   [clojure.java.shell :refer [sh]]))

(defn- check-for-gpg-command
  "Returns false if gpg command doesn't exist in the path"
  [] (-> (sh "which" "gpg") :exit (= 0) true?))

(defn decrypt-api-key
  "Reads encrypted giantbomb.com API key"
  []
  (let [cmd ["gpg" "-q" "--for-your-eyes-only"
             "--no-tty" "-d" "resources/creds.gpg"]]
    (when-not (check-for-gpg-command)
      (throw (Exception. "gpg command not found in $PATH")))
    (some->>
     cmd
     (apply sh)
     :out
     edn/read-string
     :giantbomb-api-key)))


(comment
  (check-for-gpg-command)
  (decrypt-api-key)
  )
