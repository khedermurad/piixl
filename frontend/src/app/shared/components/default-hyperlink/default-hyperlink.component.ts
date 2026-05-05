import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-default-hyperlink',
  imports: [RouterLink],
  templateUrl: './default-hyperlink.component.html',
  styleUrl: './default-hyperlink.component.scss',
})
export class DefaultHyperlinkComponent {
  title = input<string>('');
  href = input<string>('#');
  isExternal = input<boolean>(false);
}
