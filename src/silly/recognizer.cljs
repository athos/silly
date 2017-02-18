(ns silly.recognizer
  (:require [clojure.core.async :as a]))

(defn make-recognizer [ch]
  (let [rec (new js/webkitSpeechRecognition)]
    (set! (.-lang rec) "ja-JP")
    (set! (.-interimResults rec) false)
    (.addEventListener rec "result"
      (fn [e]
        (a/put! ch (-> (.-results e) (aget 0 0) (.-transcript)))))
    rec))
