import { AbilityStage } from '@kit.AbilityKit';
import 'libentry.so';
import { setup, init } from '@kuiklybase/knoi';
import BuildProfile from 'BuildProfile';

export default class EntryAbilityStage extends AbilityStage {
  onCreate(): void {
    setup('libshared.so', BuildProfile.DEBUG);
    init();
  }
}
