// ICommunicationService.aidl
package com.kashif.aidleventcommunicator.communication;

import com.kashif.aidleventcommunicator.message.IMessage;
import com.kashif.aidleventcommunicator.communication.IMessageListener;

interface ICommunicationService {
    void sendMessage(in IMessage message);
    void registerListener(IMessageListener listener);
    void unregisterListener(IMessageListener listener);
}