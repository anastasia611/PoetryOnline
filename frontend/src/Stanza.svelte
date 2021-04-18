<script>
    import { createEventDispatcher } from 'svelte';
    import Line from "./Line.svelte";
    import { push, remove } from "./common/arrays";

    export let lines = [];
    export let lineIndex = 0;
    export let wordIndex = 0;

    const REMOVE_DELAY = 50;
    const ADD_DELAY = 50;

    const dispatch = createEventDispatcher();

    const onRemoveLine = i => {
        setTimeout(() => {
            remove(lines, i);
            lines = lines;
        }, REMOVE_DELAY);
    };

    const onAddLine = i => {
        lineIndex = i;
        setTimeout(() => {
            if (!lines[lineIndex].length || !lines[lineIndex][0]) {
                return;
            }
            lines = push(lines, ++lineIndex, [ '' ]);
            wordIndex = 0;
        }, ADD_DELAY);
    };

    const onBack = () => {
        if (lineIndex > 0) {
            wordIndex = lines[--lineIndex].length - 1;
        } else {
            dispatch('back');
        }
    };

    const onNext = async () => {
        if (lineIndex < lines.length - 1) {
            lineIndex++;
            wordIndex = 0;
        } else {
            dispatch('next');
        }
    };

    const onFocus = i => {
        lineIndex = i;
        dispatch('focus');
    };

</script>

<div class="stanza">
    {#each lines as words, i}
        <Line {words}
              wordIndex={i === lineIndex ? wordIndex : -1}
              on:focus={() => onFocus(i)}
              on:removeLine={() => onRemoveLine(i)}
              on:addLine={() => onAddLine(i)}
              on:back={onBack}
              on:next={onNext}/>
    {/each}
</div>

<style lang="scss">
    .stanza {
        margin-bottom: 2rem;
        padding: 0.5rem;

        &:hover {
            background-color: #F5F5F5;
        }
    }
</style>