import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { ChatApiService, ChatMessage, ChatRoom } from '../../services/chat-api.service';
import { ChatWebSocketService } from '../../services/chat-websocket.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TimeAgoPipe } from '../../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-chat-room',
  imports: [RouterLink, ReactiveFormsModule, TranslateModule, TimeAgoPipe],
  templateUrl: './chat-room.component.html',
  styleUrl: './chat-room.component.scss',
})
export class ChatRoomComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesEnd') messagesEnd!: ElementRef;

  room: ChatRoom | null = null;
  messages: ChatMessage[] = [];
  messageControl = new FormControl('');
  roomId!: number;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private chatApi: ChatApiService,
    private wsService: ChatWebSocketService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.roomId = Number(this.route.snapshot.paramMap.get('id'));
    this.chatApi.getRoom(this.roomId).subscribe((r) => (this.room = r));
    this.chatApi.getMessages(this.roomId).subscribe((p) => {
      this.messages = p.content.reverse();
    });
    this.chatApi.markRead(this.roomId).subscribe();

    this.wsService.connect();
    this.wsService.subscribeToRoom(this.roomId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((msg) => {
        this.messages.push(msg);
        this.scrollToBottom();
      });
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  send(): void {
    const content = this.messageControl.value?.trim();
    if (!content) return;
    this.messageControl.setValue('');
    this.chatApi.send(this.roomId, content).subscribe((msg) => {
      this.messages.push(msg);
    });
  }

  sendOnEnter(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  isOwnMessage(msg: ChatMessage): boolean {
    return msg.sender.id === this.auth.currentUser?.id;
  }

  private scrollToBottom(): void {
    try {
      this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
    } catch {}
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
