<script>
    import Line from "./Line.svelte";
    import { push, remove } from "./common/arrays";

    export let lines = [];
    export let lineIndex = -1;
    export let wordIndex = -1;

    const REMOVE_DELAY = 50;
    const ADD_DELAY = 50;

    const onRemoveLine = i => {
        setTimeout(() => {
            console.log(i, lines)
            remove(lines, i);
            console.log(i, lines)
            lines = lines;
        }, REMOVE_DELAY);
    };

    const onAddLine = i => {
        setTimeout(() => {
            lines = push(lines, i + 1, ['']);
            lineIndex = i + 1;
            wordIndex = 0;
        }, ADD_DELAY);
    };

    const onBack = (e, i) => {
        console.log('bk', e, i)
        if (i > 0) {
            lineIndex = i - 1;
            wordIndex = lines[i - 1].length - 1;
        }
    };

    const onNext = async (e, i) => {
        if (i < lines.length - 1) {
            lineIndex = i + 1;
            wordIndex = 0;
        }
    };
</script>

<div class="stanza">
    {#each lines as words, i}
        <Line {words}
              editableIndex={i === lineIndex ? wordIndex : -1}
              on:removeLine={() => onRemoveLine(i)}
              on:addLine={() => onAddLine(i)}
              on:back={e => onBack(e, i)}
              on:next={e => onNext(e, i)} />
    {/each}
</div>

<style lang="scss">
    .stanza {
        margin-bottom: 2rem;
    }
</style>