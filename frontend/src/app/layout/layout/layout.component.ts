import { Component, Signal } from '@angular/core';
import { BreakpointService } from '../../core/services/breakpoint.service';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss',
})
export class LayoutComponent {
  isMobile: Signal<boolean>;
  isDesktop: Signal<boolean>;

  constructor(private breakpointServer: BreakpointService) {
    this.isMobile = breakpointServer.isMobile;
    this.isDesktop = breakpointServer.isDesktop;
  }
}
