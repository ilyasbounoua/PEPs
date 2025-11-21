import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { PageType } from '../../../models/interfaces';

@Component({
  selector: 'app-sidenav',
  imports: [CommonModule, MatListModule, MatIconModule],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
})
export class Sidenav {
  currentPage = input.required<PageType>();
  pageChange = output<PageType>();

  onPageClick(page: PageType) {
    this.pageChange.emit(page);
  }
}
