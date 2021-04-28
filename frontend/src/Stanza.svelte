<script>
    import { createEventDispatcher } from 'svelte';
    import Line from "./Line.svelte";
    import { push, remove } from "./common/arrays";

    export let lines = [];
    export let lineIndex = -1;
    export let wordIndex = -1;

    const dispatch = createEventDispatcher();

    $: {
        // console.log('stanza line ind', lineIndex, wordIndex)
    }

    const onRemoveLine = (e, i) => {
        lineIndex = i;
        lines[lineIndex] = e.detail.words;
        console.log('rm line', lines, i)
        remove(lines, i);
        lines = lines;
        if (!lines.length) {
            onRemoveStanza();
        } else {
            onBack();
        }
    };

    const onRemoveStanza = () => {
        console.log('rm stanza', lines)
        dispatch('remove');
    };

    const onAddLine = (e, i) => {
        lineIndex = i;
        lines[lineIndex] = e.detail.words;
        console.log('add line', lines[lineIndex])
        if (!lines[lineIndex] || !lines[lineIndex].length || !lines[lineIndex][0]) {
            console.log('rm line', lines[lineIndex], i)
            onRemoveLine(e, i);
            dispatch('add', { lines });
        } else {
            lines = push(lines, ++lineIndex, [ '' ]);
            wordIndex = 0;
        }
    };

    const onBack = () => {
        console.log('back to line', lines, lineIndex)
        if (lineIndex > 0) {
            wordIndex = lines[--lineIndex].length - 1;
        } else {
            dispatch('back', { lines });
        }
    };

    const onNext = async () => {
        console.log('next line', lines, lineIndex)
        if (lineIndex < lines.length - 1) {
            lineIndex++;
            wordIndex = 0;
        } else {
            dispatch('next');
        }
    };

    const onFocus = (e, i) => {
        console.log('foc line', i)
        lineIndex = i;
        wordIndex = e.detail.wordIndex;
        dispatch('focus', { wordIndex, lineIndex });
    };

    const onBlur = (i) => {
        console.log('blur st, line:', i)
        dispatch('blur');
    };

</script>

<div class="stanza">
    {#each lines as words, i}
        <Line {words}
              wordIndex={i === lineIndex ? wordIndex : -1}
              on:focus={e => onFocus(e, i)}
              on:blur={() => onBlur(i)}
              on:remove={e => onRemoveLine(e, i)}
              on:add={e => onAddLine(e, i)}
              on:back={onBack}
              on:next={onNext}/>
    {/each}
</div>

<style lang="scss">
    .stanza {
        margin-bottom: 2rem;
        padding: 0.5rem;

        &:hover, &:focus-within {
            background-color: #F5F5F5;
        }
    }
</style>