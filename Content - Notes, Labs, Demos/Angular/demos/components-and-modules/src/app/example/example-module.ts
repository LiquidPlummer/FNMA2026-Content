import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OldFashionedComponent } from '../old-fashioned-component/old-fashioned-component';
import { NewStyleComponent } from '../new-style-component/new-style-component';


/*
The "Old fashioned component" is a non-standalone component. As of Angular v17, standalone components are the default. We made that one
not standalone by setting "standalone" to "false" in the component decorator. The other, NewStyleComponent is standalone, which
is the more common type going forward. The old fashioned ones are slowly being deprecated out of the framework.

We can't "import" non-standalone components, we have to "declare" them. 
We can't "declare" standalone components, we have to "import" them.
Either way, to then make either available to anything that imports our module, we export them all.
In this way we are building up a grouping of components that are part of the module. This allows us to import just the module and gain
access to all of these members.
*/
@NgModule({
  declarations: [OldFashionedComponent],
  imports: [NewStyleComponent],
  exports: [NewStyleComponent, OldFashionedComponent]
})
export class ExampleModule { }
