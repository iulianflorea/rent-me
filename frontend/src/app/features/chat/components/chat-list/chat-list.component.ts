import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ChatApiService, ChatRoom } from '../../services/chat-api.service';
import { TimeAgoPipe } from '../../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-chat-list',
  imports: [RouterLink, TranslateModule, TimeAgoPipe],
  templateUrl: './chat-list.component.html',
  styleUrl: './chat-list.component.scss',
})
export class ChatListComponent implements OnInit {
  rooms: ChatRoom[] = [];
  loading = true;

  constructor(private chatApi: ChatApiService) {}

  ngOnInit(): void {
    this.chatApi.getRooms().subscribe({
      next: (r) => { this.rooms = r; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }
}
