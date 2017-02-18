(ns silly.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [ajax.core :as ajax]
            [clojure.core.async :as a]))

(enable-console-print!)

(defn- log [x]
  (js/console.log x)
  x)

(def LUIS_API_URL "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/837d708c-46a1-4612-b750-74b831dd2d94")
(def LUIS_SUBSCRIPTION_KEY "a5c45cac66734a1d9c34cb4bddd243b8")

(defn analyze [message callback]
  (ajax/GET LUIS_API_URL
            {:handler callback
             :response-format :json
             :params {:subscription-key LUIS_SUBSCRIPTION_KEY
                      :q message}
             :keywords? true}))

(defn make-answer [result]
  (let [{:keys [intent score]} (:topScoringIntent result)]
    (or (when (> score 0.5)
          (case (keyword intent)
            :greeting "こんにちは"
            :ask_weather "天気くらい自分で調べてね"
            :ask_lookup "それは自分で調べてね"
            nil))
        "ちょっと何言ってるか分かりませんね")))

(defn make-recognizer []
  (let [rec (new js/webkitSpeechRecognition)]
    (set! (.-lang rec) "ja-JP")
    (set! (.-interimResults rec) false)
    rec))

(defn speak [message callback]
  (let [utterance (new js/SpeechSynthesisUtterance)]
    (set! (.-lang utterance) "ja-JP")
    (set! (.-text utterance) message)
    (.addEventListener utterance "end" callback)
    (js/speechSynthesis.speak utterance)))

(defn- ->chan [f & args]
  (let [ch (a/chan)]
    (apply f (concat args [#(a/put! ch %)]))
    ch))

(defn main-loop [ch recognizer]
  (log "main loop started")
  (go-loop []
    (log "waiting for message ...")
    (let [message (log (a/<! ch))
          answer (make-answer (a/<! (->chan analyze message)))]
      (a/<! (->chan speak answer))
      (.start recognizer)
      (recur))))

(defn -main []
  (let [ch (a/chan)
        rec (make-recognizer)]
    (.addEventListener rec "result"
      (fn [e]
        (a/put! ch (-> (.-results e) (aget 0 0) (.-transcript)))))
    (.addEventListener rec "end" #(.stop rec))

    (main-loop ch rec)
    (.start rec)))

(.addEventListener js/window "load" -main)

(defn on-js-reload [])
