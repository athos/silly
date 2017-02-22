(ns silly.speaker)

(set! *warn-on-infer* true)

(defn speak [message callback]
  (let [utterance (new js/SpeechSynthesisUtterance)]
    (set! (.-lang utterance) "ja-JP")
    (set! (.-text utterance) message)
    (.addEventListener utterance "end" callback)
    (js/speechSynthesis.speak utterance)))
