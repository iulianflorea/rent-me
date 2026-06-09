import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Endpoints } from '../../../api/endpoints';

export interface ChatRoom {
  id: number;
  rental: { id: number; listing: { title: string; firstImageUrl: string } } | null;
  otherUser: { id: number; firstName: string; lastName: string; kycStatus: string };
  lastMessage: string | null;
  lastMessageAt: string | null;
  unreadCount: number;
}

export interface ChatMessage {
  id: number;
  sender: { id: number; firstName: string; lastName: string };
  content: string;
  fileUrl: string | null;
  messageType: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ChatApiService {
  constructor(private http: HttpClient) {}

  getRooms(): Observable<ChatRoom[]> {
    return this.http.get<ChatRoom[]>(Endpoints.chat.rooms);
  }

  getRoom(id: number): Observable<ChatRoom> {
    return this.http.get<ChatRoom>(Endpoints.chat.roomById(id));
  }

  getMessages(roomId: number, page = 0, size = 50): Observable<{ content: ChatMessage[]; totalElements: number }> {
    return this.http.get<{ content: ChatMessage[]; totalElements: number }>(
      Endpoints.chat.messages(roomId), { params: { page, size } }
    );
  }

  send(roomId: number, content: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(Endpoints.chat.send, { roomId, content, messageType: 'TEXT' });
  }

  sendFile(roomId: number, file: File): Observable<ChatMessage> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ChatMessage>(Endpoints.chat.sendFile(roomId), form);
  }

  markRead(roomId: number): Observable<void> {
    return this.http.post<void>(Endpoints.chat.markRead(roomId), {});
  }
}
