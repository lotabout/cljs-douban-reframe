(ns cljs-douban-reframe.handlers
    (:require [re-frame.core :as re-frame]
              [cljs-douban-reframe.db :as db]
              [cljs-douban-reframe.rpc :as rpc]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/register-handler
 :query-channel
 (fn [db _]
   "send request for channel list"
   (rpc/get-channel-list)
   db))

(re-frame/register-handler
 :receive-channel
 (fn [db [_ channel-list]]
   "save the channel list to the database"
   (assoc db :channel-list (channel-list "channels"))))
