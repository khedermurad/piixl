import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-default-button',
  imports: [],
  templateUrl: './default-button.component.html',
  styleUrl: './default-button.component.scss',
})
export class DefaultButtonComponent {
  title = input<string>('');
  buttonClicked = output<void>();
  variant = input<'solid' | 'outline'>('solid');
  type = input<'button' | 'submit'>('button');

  public onClicked() {
    this.buttonClicked.emit();
  }
}
