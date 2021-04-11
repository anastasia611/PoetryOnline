<script>
    import Line from "./Line.svelte";
    import { push, remove } from "./common/arrays";

    export let lines = [];
    export let editableIndex = -1;

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
            lines = push(lines, i + 1, []);
        }, ADD_DELAY);
    };

    const onBack = (e, i) => {
        if (i > 0) {
            editableIndex = i - 1;
        }
    };

    const onNext = async (e, i) => {
        if (i < lines.length - 1) {
            editableIndex = i + 1;
        }
    };
</script>

<div class="stanza">
    {#each lines as words, i}
        <Line {words}
              editableIndex={i === editableIndex ? words.length - 1 : -1}
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