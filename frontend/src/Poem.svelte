<script>
    import { createEventDispatcher } from 'svelte';
    import Stanza from "./Stanza.svelte";
    import { push, remove } from "./common/arrays";

    export let stanzas = [];
    export let stanzaIndex = -1;
    export let lineIndex = -1;
    export let wordIndex = -1;

    const dispatch = createEventDispatcher();

    const onRemoveStanza = i => {
        stanzaIndex = i;
        console.log('rm stanza', stanzas, i)
        remove(stanzas, i);
        stanzas = stanzas;
    };

    const onAddStanza = (e, i) => {
        stanzaIndex = i;
        stanzas[stanzaIndex] = e.detail.lines;
        console.log('add st', i, stanzas[i])
        if (stanzas[stanzaIndex] && stanzas[stanzaIndex].length && stanzas[stanzaIndex][0]) {
            stanzas = push(stanzas, ++stanzaIndex, [ [ '' ] ]);
            lineIndex = 0;
            wordIndex = 0;
        }
    };

    const onNext = () => {
        if (stanzaIndex < stanzas.length - 1) {
            stanzaIndex++;
            lineIndex = 0;
            wordIndex = 0;
            console.log('nxt')
        }
    };

    const onBack = e => {
        // stanzas[stanzaIndex] = e.detail.lines;
        if (stanzaIndex > 0) {
            const stanza = stanzas[--stanzaIndex];
            lineIndex = stanza.length - 1;
            const line = stanza[lineIndex];
            wordIndex = line.length - 1;
            console.log('bc', stanzaIndex, lineIndex, wordIndex)
        }
    };

    const onFocus = (e, i) => {
        stanzaIndex = i;
        lineIndex = e.detail.lineIndex;
        wordIndex = e.detail.wordIndex;
        dispatch('focus');
    };

    const onBlur = () => {
        dispatch('blur');
    };
</script>

<div class="poem">
    <h2 contenteditable>Стихотворение</h2>
    {#each stanzas as lines, i}
        <Stanza
                {lines}
                wordIndex={i === stanzaIndex ? wordIndex : -1}
                lineIndex={i === stanzaIndex ? lineIndex : -1}
                on:focus={e => onFocus(e, i)}
                on:blur={onBlur}
                on:add={e => onAddStanza(e, i)}
                on:remove={() => onRemoveStanza(i)}
                on:back={onBack}
                on:next={onNext}/>
    {/each}
</div>

<style lang="scss">
    .poem {
        display: flex;
        width: fit-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 1rem;
    }

    h2 {
        margin-left: 0.75rem;
    }
</style>