(ns konin.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [clojure.data.json :as json]))

(defn bytes->map [^bytes payload]
  (-> payload (String. "UTF-8") (json/read-str :key-fn keyword)))

(defn gen-queue-name [prefix]
  (str prefix "_rpc_queue"))

(defn make-basic-server [prefix]
  (let [queue-name (gen-queue-name prefix)
        conn (rmq/connect)
        ch (lch/open conn)]
    (lq/declare ch queue-name)
    {:conn conn :ch ch :queue-name queue-name}))

(defn apply-fn [{:keys [interface function args]} handler-map]
  (let [iface-fn (keyword interface)
        f-fn (keyword function)]
    (-> handler-map
        iface-fn
        f-fn
        (apply args))))

(defn wrap-result [result]
  {:result result})

(defn server-message-handler [handler-map]
  (fn [ch {:keys [reply-to correlation-id]} payload]
    (let [data (bytes->map payload)
          result (-> data
                     (apply-fn handler-map)
                     wrap-result
                     json/write-str)]
      (lb/publish ch "" reply-to result :correlation-id correlation-id))))

(defn start-server-consumer [serv handler-map]
  (lc/subscribe (:ch serv) (:queue-name serv) (server-message-handler handler-map))
  serv)

(defn make-server [prefix handler-map]
  (-> prefix
      make-basic-server
      (start-server-consumer handler-map)))
