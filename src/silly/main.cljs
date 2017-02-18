(ns silly.main
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [ajax.core :as ajax]
            [clojure.core.async :as a]
            [integrant.core :as ig]
            [silly.answer :as answer]
            [silly.config :as config]
            [silly.recognizer :as recognizer]
            [silly.speaker :as speaker]))

(enable-console-print!)

(defmethod ig/init-key :message-chan [_ _]
  (a/chan))

(defmethod ig/halt-key! :message-chan [_ ch]
  (a/close! ch))

(defmethod ig/init-key :recognizer [_ {ch :message-chan}]
  (recognizer/make-recognizer ch))

(defmethod ig/halt-key! :recognizer [_ recognizer]
  (js/console.log recognizer)
  (.stop recognizer))

(defmethod ig/init-key :analyzer [_ {:keys [api-url luis-key]}]
  (answer/make-analyzer api-url luis-key))

(defn- log [x]
  (js/console.log x)
  x)

(defn- ->chan [f & args]
  (let [ch (a/chan)]
    (apply f (concat args [#(a/put! ch %)]))
    ch))

(defmethod ig/init-key :app [_ opts]
  (let [{ch :message-chan :keys [recognizer analyzer]} opts]
    (.start recognizer)
    (log "main loop started")
    (go-loop []
      (log "waiting for message ...")
      (when-let [message (log (a/<! ch))]
        (.stop recognizer)
        (let [answer (a/<! (->chan answer/answer analyzer message))]
          (a/<! (->chan speaker/speak answer))
          (.start recognizer)
          (recur))))))

(defonce system (atom nil))

(defn start []
  (reset! system (ig/init config/config)))

(defn stop []
  (ig/halt! @system))

(defn on-js-reload []
  (stop)
  (start))

(.addEventListener js/window "load" start)
