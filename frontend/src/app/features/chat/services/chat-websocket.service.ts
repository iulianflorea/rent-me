import { Injectable, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { ChatMessage } from './chat-api.service';

@Injectable({ providedIn: 'root' })
export class ChatWebSocketService implements OnDestroy {
  private client: Client | null = null;
  private messageSubjects = new Map<number, Subject<ChatMessage>>();

  constructor(private auth: AuthService) {}

  connect(): void {
    const token = this.auth.getToken();
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
    });
    this.client.activate();
  }

  subscribeToRoom(roomId: number): Subject<ChatMessage> {
    if (!this.messageSubjects.has(roomId)) {
      const subject = new Subject<ChatMessage>();
      this.messageSubjects.set(roomId, subject);

      const subscribe = (): void => {
        if (this.client?.connected) {
          this.client.subscribe(`/topic/chat/${roomId}`, (msg: IMessage) => {
            subject.next(JSON.parse(msg.body) as ChatMessage);
          });
        } else {
          setTimeout(subscribe, 500);
        }
      };
      subscribe();
    }
    return this.messageSubjects.get(roomId)!;
  }

  sendMessage(roomId: number, content: string): void {
    this.client?.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ roomId, content }),
    });
  }

  sendTyping(roomId: number): void {
    this.client?.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({ roomId }),
    });
  }

  disconnect(): void {
    this.client?.deactivate();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
